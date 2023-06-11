package io.github.davidqf555.minecraft.f1040.common.entities;

import io.github.davidqf555.minecraft.f1040.common.ServerConfigs;
import io.github.davidqf555.minecraft.f1040.common.world.data.GovernmentData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class GiveLootGoal extends Goal {

    private final TaxCollectorEntity mob;
    private final double range, speed, within;
    private Player target;

    public GiveLootGoal(TaxCollectorEntity mob, double range, double speed) {
        this.mob = mob;
        this.range = range;
        this.speed = speed;
        within = 2;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (mob.hasPayed()) {
            return false;
        }
        target = null;
        for (Player player : mob.level().getEntitiesOfClass(Player.class, AABB.ofSize(mob.position(), range * 2, range * 2, range * 2))) {
            if (GovernmentData.isCorrupted((ServerLevel) mob.level(), mob.getGovID(), player.getUUID())) {
                target = player;
                break;
            }
        }
        if (target == null) {
            return false;
        }
        return !entitiesNearby();
    }

    @Override
    public void start() {
        mob.getNavigation().moveTo(target, speed);
    }

    @Override
    public void tick() {
        if (mob.distanceToSqr(target) <= within * within) {
            pay();
            mob.handleEntityEvent((byte) 56);
            mob.setPayed(true);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !mob.getNavigation().isDone() && !mob.hasPayed() && !entitiesNearby();
    }

    protected boolean entitiesNearby() {
        for (Player player : mob.level().getEntitiesOfClass(Player.class, AABB.ofSize(target.position(), range * 2, range * 2, range * 2))) {
            if (!player.equals(target) && player.hasLineOfSight(target)) {
                return true;
            }
        }
        return false;
    }

    protected void pay() {
        Vec3 dir = target.position().subtract(mob.getEyePosition(1)).normalize();
        List<ItemStack> items = GovernmentData.removeRandom((ServerLevel) mob.level(), mob.getGovID(), mob.getRandom(), ServerConfigs.INSTANCE.lootProportion.get());
        for (ItemStack stack : items) {
            ItemEntity item = new ItemEntity(mob.level(), mob.getX(), mob.getEyeY(), mob.getZ(), stack);
            item.setDeltaMovement(item.getDeltaMovement().add(dir));
            mob.level().addFreshEntity(item);
        }
    }

}