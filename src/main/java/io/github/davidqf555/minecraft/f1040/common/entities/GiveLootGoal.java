package io.github.davidqf555.minecraft.f1040.common.entities;

import io.github.davidqf555.minecraft.f1040.common.ServerConfigs;
import io.github.davidqf555.minecraft.f1040.common.world.data.GovernmentData;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.EnumSet;
import java.util.List;

public class GiveLootGoal extends Goal {

    private final TaxCollectorEntity mob;
    private final double range, speed, within;
    private PlayerEntity target;

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
        for (PlayerEntity player : mob.level.getLoadedEntitiesOfClass(PlayerEntity.class, AxisAlignedBB.ofSize(range * 2, range * 2, range * 2).move(mob.position()))) {
            if (GovernmentData.isCorrupted((ServerWorld) mob.level, mob.getGovID(), player.getUUID())) {
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
        for (PlayerEntity player : mob.level.getLoadedEntitiesOfClass(PlayerEntity.class, AxisAlignedBB.ofSize(range * 2, range * 2, range * 2).move(target.position()))) {
            if (!player.equals(target) && player.canSee(target)) {
                return true;
            }
        }
        return false;
    }

    protected void pay() {
        Vector3d dir = target.position().subtract(mob.getEyePosition(1)).normalize();
        List<ItemStack> items = GovernmentData.removeRandom((ServerWorld) mob.level, mob.getGovID(), mob.getRandom(), ServerConfigs.INSTANCE.lootProportion.get());
        for (ItemStack stack : items) {
            ItemEntity item = new ItemEntity(mob.level, mob.getX(), mob.getEyeY(), mob.getZ(), stack);
            item.setDeltaMovement(item.getDeltaMovement().add(dir));
            mob.level.addFreshEntity(item);
        }
    }

}