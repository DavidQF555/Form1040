package io.github.davidqf555.minecraft.f1040.common.entities;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;

import java.util.EnumSet;

public class FollowPlayersGoal extends Goal {

    private final MobEntity mob;
    private final double speedModifier;
    private final PathNavigator navigation;
    private final float stopDistance;
    private final float areaSize;
    private PlayerEntity followingMob;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public FollowPlayersGoal(MobEntity mob, double speed, float stopDist, float dist) {
        this.mob = mob;
        speedModifier = speed;
        navigation = mob.getNavigation();
        stopDistance = stopDist;
        areaSize = dist;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        PlayerEntity player = mob.level.getNearestPlayer(mob, areaSize);
        if (player != null) {
            followingMob = player;
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return followingMob != null && !navigation.isDone() && mob.distanceToSqr(followingMob) > (double) (stopDistance * stopDistance);
    }

    @Override
    public void start() {
        timeToRecalcPath = 0;
        oldWaterCost = mob.getPathfindingMalus(PathNodeType.WATER);
        mob.setPathfindingMalus(PathNodeType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        followingMob = null;
        navigation.stop();
        mob.setPathfindingMalus(PathNodeType.WATER, oldWaterCost);
    }

    @Override
    public void tick() {
        if (followingMob != null && !mob.isLeashed()) {
            mob.getLookControl().setLookAt(followingMob, 10, mob.getMaxHeadXRot());
            if (--timeToRecalcPath <= 0) {
                timeToRecalcPath = 10;
                double distSq = mob.distanceToSqr(followingMob);
                if (distSq > stopDistance * stopDistance) {
                    navigation.moveTo(followingMob, speedModifier);
                } else {
                    navigation.stop();
                    if (distSq <= stopDistance) {
                        navigation.moveTo(mob.getX() * 2 - followingMob.getX(), mob.getY(), mob.getZ() * 2 - followingMob.getZ(), speedModifier);
                    }

                }
            }
        }
    }
}