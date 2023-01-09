package io.github.davidqf555.minecraft.f1040.common.entities;

import io.github.davidqf555.minecraft.f1040.common.player.Debt;
import io.github.davidqf555.minecraft.f1040.common.world.data.GovernmentData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.server.ServerWorld;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public class TargetIndebtedGoal<T extends MobEntity> extends TargetGoal {

    private Optional<PlayerEntity> target;

    public TargetIndebtedGoal(T mob, boolean mustSee) {
        super(mob, mustSee);
        target = Optional.empty();
    }

    @Override
    public boolean canUse() {
        AxisAlignedBB bounds = mob.getBoundingBox().inflate(getFollowDistance());
        Stream<PlayerEntity> stream = mob.level.getLoadedEntitiesOfClass(PlayerEntity.class, bounds, EntityPredicates.NO_CREATIVE_OR_SPECTATOR).stream().filter(Debt::isIndebted);
        if (mob instanceof TaxCollectorEntity) {
            int id = ((TaxCollectorEntity) mob).getGovID();
            stream = stream.filter(player -> !GovernmentData.isCorrupted((ServerWorld) mob.level, id, player.getUUID()));
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
