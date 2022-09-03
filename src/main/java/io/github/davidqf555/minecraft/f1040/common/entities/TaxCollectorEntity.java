package io.github.davidqf555.minecraft.f1040.common.entities;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.ServerConfigs;
import io.github.davidqf555.minecraft.f1040.common.packets.OpenTaxScreenPacket;
import io.github.davidqf555.minecraft.f1040.common.player.Debt;
import io.github.davidqf555.minecraft.f1040.registration.TagRegistry;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Random;

public class TaxCollectorEntity extends CreatureEntity implements INPC {

    private PlayerEntity trading;

    public TaxCollectorEntity(EntityType<TaxCollectorEntity> type, World world) {
        super(type, world);
        setItemInHand(Hand.MAIN_HAND, Items.IRON_AXE.getDefaultInstance());
    }

    public static AttributeModifierMap.MutableAttribute createMobAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35F);
    }

    @Nullable
    public static <T extends LivingEntity> T spawn(PlayerEntity player, EntityType<T> type, int min, int max) {
        BlockPos center = player.blockPosition();
        Random random = player.getRandom();
        for (int i = 0; i < 10; i++) {
            int x = center.getX() + (random.nextInt(max - min + 1) + min) * (random.nextBoolean() ? -1 : 1);
            int z = center.getZ() + (random.nextInt(max - min + 1) + min) * (random.nextBoolean() ? -1 : 1);
            int y = player.level.getHeight(Heightmap.Type.WORLD_SURFACE, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (WorldEntitySpawner.isSpawnPositionOk(EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, player.level, pos, type)) {
                T entity = type.create(player.level);
                if (entity != null) {
                    Vector3d vec = Vector3d.atBottomCenterOf(pos);
                    entity.setPos(vec.x(), vec.y(), vec.z());
                    player.level.addFreshEntity(entity);
                    return entity;
                }
            }
        }
        return null;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new SwimGoal(this));
        goalSelector.addGoal(1, new LookAtPayerGoal());
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1, true));
        goalSelector.addGoal(3, new FollowPlayersGoal(this, 1, 4, 16));
        goalSelector.addGoal(4, new LookAtGoal(this, PlayerEntity.class, 6));
        goalSelector.addGoal(5, new LookRandomlyGoal(this));
        targetSelector.addGoal(0, new HurtByTargetGoal(this));
        targetSelector.addGoal(1, new TargetIndebtedGoal<>(this, true));
    }

    @Override
    public ActionResultType interactAt(PlayerEntity player, Vector3d hit, Hand hand) {
        if (player instanceof ServerPlayerEntity && hand == Hand.MAIN_HAND) {
            NonNullList<ItemStack> tax = NonNullList.create();
            Debt debt = Debt.get(player);
            debt.getAllDebt().forEach(item -> {
                ItemStack stack = item.getDefaultInstance();
                stack.setCount(debt.getDebt(item));
                tax.add(stack);
            });
            if (!tax.isEmpty()) {
                ItemStack item = player.getItemInHand(hand);
                if (!item.isEmpty() && TagRegistry.BRIBE.contains(item.getItem())) {
                    if (!player.isCreative()) {
                        item.shrink(1);
                    }
                    if (player.getRandom().nextDouble() < ServerConfigs.INSTANCE.bribeSuccessRate.get()) {
                        Debt.get(player).clear();
                        level.broadcastEntityEvent(this, (byte) 0);
                    } else {
                        Debt.add(player);
                        level.broadcastEntityEvent(this, (byte) 1);
                    }
                    return ActionResultType.CONSUME;
                } else {
                    setTradingPlayer(player);
                    Form1040.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new OpenTaxScreenPacket(tax, Debt.canPay(player), getUUID()));
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return super.interactAt(player, hit, hand);
    }

    @Nullable
    public PlayerEntity getTradingPlayer() {
        return trading;
    }

    public void setTradingPlayer(@Nullable PlayerEntity player) {
        trading = player;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (tickCount >= ServerConfigs.INSTANCE.taxPeriod.get()) {
            remove();
        }
    }

    @Override
    public void handleEntityEvent(byte val) {
        if (val == 0) {
            for (int i = 0; i < 7; i++) {
                double dX = random.nextGaussian() * 0.02;
                double dY = random.nextGaussian() * 0.02;
                double dZ = random.nextGaussian() * 0.02;
                level.addParticle(ParticleTypes.HAPPY_VILLAGER, getRandomX(1), getRandomY() + 0.5, getRandomZ(1), dX, dY, dZ);
            }
        } else if (val == 1) {
            for (int i = 0; i < 7; i++) {
                double dX = random.nextGaussian() * 0.02;
                double dY = random.nextGaussian() * 0.02;
                double dZ = random.nextGaussian() * 0.02;
                level.addParticle(ParticleTypes.ANGRY_VILLAGER, getRandomX(1), getRandomY() + 0.5, getRandomZ(1), dX, dY, dZ);
            }
        } else {
            super.handleEntityEvent(val);
        }

    }

    private class LookAtPayerGoal extends LookAtGoal {

        public LookAtPayerGoal() {
            super(TaxCollectorEntity.this, PlayerEntity.class, 8);
        }

        public boolean canUse() {
            PlayerEntity player = getTradingPlayer();
            if (player == null) {
                return false;
            } else {
                lookAt = player;
                return true;
            }
        }
    }
}
