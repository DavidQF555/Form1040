package io.github.davidqf555.minecraft.f1040.common.entities;

import io.github.davidqf555.minecraft.f1040.common.Debt;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Comparator;
import java.util.Optional;

public class TargetIndebtedGoal<T extends MobEntity> extends TargetGoal {

    private Optional<PlayerEntity> target;

    public TargetIndebtedGoal(T mob, boolean mustSee) {
        super(mob, mustSee);
        target = Optional.empty();
    }

    @Override
    public boolean canUse() {
        AxisAlignedBB bounds = mob.getBoundingBox().inflate(getFollowDistance());
        target = mob.level.getLoadedEntitiesOfClass(PlayerEntity.class, bounds, EntityPredicates.NO_CREATIVE_OR_SPECTATOR).stream().filter(Debt::isIndebted).min(Comparator.comparingDouble(mob::distanceToSqr));
        return target.isPresent();
    }

    @Override
    public void start() {
        mob.setTarget(target.get());
        super.start();
    }
}
