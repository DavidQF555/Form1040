package io.github.davidqf555.minecraft.f1040.common.entities;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import java.util.EnumSet;

public class FollowPlayersGoal extends Goal {

    private final Mob mob;
    private final double speedModifier;
    private final PathNavigation navigation;
    private final float stopDistance;
    private final float areaSize;
    private Player followingMob;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public FollowPlayersGoal(Mob mob, double speed, float stopDist, float dist) {
        this.mob = mob;
        speedModifier = speed;
        navigation = mob.getNavigation();
        stopDistance = stopDist;
        areaSize = dist;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        Player player = mob.level.getNearestPlayer(mob, areaSize);
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
        oldWaterCost = mob.getPathfindingMalus(BlockPathTypes.WATER);
        mob.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    @Override
    public void stop() {
        followingMob = null;
        navigation.stop();
        mob.setPathfindingMalus(BlockPathTypes.WATER, oldWaterCost);
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