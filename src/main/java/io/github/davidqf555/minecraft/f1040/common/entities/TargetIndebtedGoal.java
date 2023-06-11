package io.github.davidqf555.minecraft.f1040.common.entities;

import io.github.davidqf555.minecraft.f1040.common.player.Debt;
import io.github.davidqf555.minecraft.f1040.common.world.data.GovernmentData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public class TargetIndebtedGoal<T extends Mob> extends TargetGoal {

    private Optional<Player> target;

    public TargetIndebtedGoal(T mob, boolean mustSee) {
        super(mob, mustSee);
        target = Optional.empty();
    }

    @Override
    public boolean canUse() {
        AABB bounds = mob.getBoundingBox().inflate(getFollowDistance());
        Stream<Player> stream = mob.level().getEntitiesOfClass(Player.class, bounds, EntitySelector.NO_CREATIVE_OR_SPECTATOR).stream().filter(Debt::isIndebted);
        if (mob instanceof TaxCollectorEntity) {
            int id = ((TaxCollectorEntity) mob).getGovID();
            stream = stream.filter(player -> !GovernmentData.isCorrupted((ServerLevel) mob.level(), id, player.getUUID()));
        }
        target = stream.min(Comparator.comparingDouble(mob::distanceToSqr));
        return target.isPresent();
    }

    @Override
    public boolean canContinueToUse() {
        return Debt.isIndebted(target.get()) && super.canContinueToUse();
    }

    @Override
    public void start() {
        mob.setTarget(target.get());
        super.start();
    }

}
