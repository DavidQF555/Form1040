package io.github.davidqf555.minecraft.f1040.common.entities;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.ServerConfigs;
import io.github.davidqf555.minecraft.f1040.common.packets.OpenTaxScreenPacket;
import io.github.davidqf555.minecraft.f1040.common.player.Debt;
import io.github.davidqf555.minecraft.f1040.common.player.GovernmentRelations;
import io.github.davidqf555.minecraft.f1040.common.world.data.GovernmentData;
import io.github.davidqf555.minecraft.f1040.registration.TagRegistry;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class TaxCollectorEntity extends CreatureEntity implements INPC {

    private PlayerEntity trading;
    private int govID;
    private boolean payed;

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
    public void tick() {
        super.tick();
        if (tickCount >= ServerConfigs.INSTANCE.taxPeriod.get()) {
            remove();
        }
    }

    @Nullable
    @Override
    public ILivingEntityData finalizeSpawn(IServerWorld world, DifficultyInstance difficulty, SpawnReason spawn, @Nullable ILivingEntityData data, @Nullable CompoundNBT tag) {
        setGovID(getRandom().nextInt(GovernmentData.MAX));
        setCustomName(GovernmentData.getName(getGovID()));
        return super.finalizeSpawn(world, difficulty, spawn, data, tag);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new SwimGoal(this));
        goalSelector.addGoal(1, new LookAtPayerGoal());
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1, true));
        goalSelector.addGoal(3, new GiveLootGoal(this, 32, 1));
        goalSelector.addGoal(4, new FollowPlayersGoal(this, 1, 4, 16));
        goalSelector.addGoal(5, new LookAtGoal(this, PlayerEntity.class, 6));
        goalSelector.addGoal(6, new LookRandomlyGoal(this));
        targetSelector.addGoal(0, new HurtByTargetGoal(this));
        targetSelector.addGoal(1, new TargetIndebtedGoal<>(this, true));
    }

    public int getGovID() {
        return govID;
    }

    public void setGovID(int id) {
        govID = id;
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
                    if (player.getRandom().nextDouble() < getBribeRate((ServerPlayerEntity) player)) {
                        Debt.get(player).clear();
                        GovernmentData.multiplyShare(player.getServer(), getGovID(), player.getUUID(), 1.5);
                        level.broadcastEntityEvent(this, (byte) 0);
                    } else {
                        Debt.add(player, getTaxRate((ServerPlayerEntity) player));
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

    public boolean hasPayed() {
        return payed;
    }

    public void setPayed(boolean payed) {
        this.payed = payed;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (tickCount >= ServerConfigs.INSTANCE.taxPeriod.get()) {
            remove();
        }
    }

    public double getTaxRate(ServerPlayerEntity player) {
        double share = GovernmentData.getRelativeShare(player.getServer(), govID, player.getUUID());
        double base = ServerConfigs.INSTANCE.taxRate.get() * GovernmentRelations.get(player).getTaxFactor();
        return base / share;
    }

    public double getBribeRate(ServerPlayerEntity player) {
        double share = GovernmentData.getRelativeShare(player.getServer(), govID, player.getUUID());
        double base = ServerConfigs.INSTANCE.bribeSuccessRate.get();
        return Math.min(1, base * (0.5 + share));
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
        } else if (val == 56) {
            for (int i = 0; i < 7; i++) {
                double dX = random.nextGaussian() * 0.02;
                double dY = random.nextGaussian() * 0.02;
                double dZ = random.nextGaussian() * 0.02;
                level.addParticle(ParticleTypes.SQUID_INK, getRandomX(1), getRandomY() + 0.5, getRandomZ(1), dX, dY, dZ);
            }
        } else {
            super.handleEntityEvent(val);
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundNBT tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GovID", getGovID());
        tag.putBoolean("Payed", hasPayed());
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("GovID", Constants.NBT.TAG_INT)) {
            setGovID(tag.getInt("GovID"));
        }
        if (tag.contains("Payed", Constants.NBT.TAG_BYTE)) {
            setPayed(tag.getBoolean("Payed"));
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean player) {
        super.dropCustomDeathLoot(source, looting, player);
        if (player && level instanceof ServerWorld) {
            List<ItemStack> items = GovernmentData.removeRandom((ServerWorld) level, getGovID(), getRandom(), ServerConfigs.INSTANCE.inventoryDropProportion.get());
            items.forEach(this::spawnAtLocation);
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        LivingEntity credit = getKillCredit();
        if (credit instanceof PlayerEntity) {
            GovernmentRelations relations = GovernmentRelations.get((PlayerEntity) credit);
            relations.setTaxFactor(relations.getTaxFactor() * ServerConfigs.INSTANCE.taxIncreaseRate.get());
            GovernmentData.multiplyShare(getServer(), getGovID(), credit.getUUID(), 0.5);
        }
    }

    private class LookAtPayerGoal extends LookAtGoal {

        public LookAtPayerGoal() {
            super(TaxCollectorEntity.this, PlayerEntity.class, 8);
        }

        @Override
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
