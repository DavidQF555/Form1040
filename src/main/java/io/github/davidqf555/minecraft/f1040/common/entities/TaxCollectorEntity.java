package io.github.davidqf555.minecraft.f1040.common.entities;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.ServerConfigs;
import io.github.davidqf555.minecraft.f1040.common.packets.OpenTaxScreenPacket;
import io.github.davidqf555.minecraft.f1040.common.player.Debt;
import io.github.davidqf555.minecraft.f1040.common.player.GovernmentRelations;
import io.github.davidqf555.minecraft.f1040.common.world.data.GovernmentData;
import io.github.davidqf555.minecraft.f1040.registration.TagRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;

public class TaxCollectorEntity extends PathfinderMob implements Npc {

    private Player trading;
    private int govID;
    private boolean payed;

    public TaxCollectorEntity(EntityType<? extends TaxCollectorEntity> type, Level world) {
        super(type, world);
        setItemInHand(InteractionHand.MAIN_HAND, Items.IRON_AXE.getDefaultInstance());
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35F);
    }

    @Nullable
    public static <T extends LivingEntity> T spawn(Player player, EntityType<T> type, int min, int max) {
        BlockPos center = player.blockPosition();
        RandomSource random = player.getRandom();
        for (int i = 0; i < 10; i++) {
            int x = center.getX() + (random.nextInt(max - min + 1) + min) * (random.nextBoolean() ? -1 : 1);
            int z = center.getZ() + (random.nextInt(max - min + 1) + min) * (random.nextBoolean() ? -1 : 1);
            int y = player.level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, player.level, pos, type)) {
                T entity = type.create(player.level);
                if (entity != null) {
                    Vec3 vec = Vec3.atBottomCenterOf(pos);
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
            remove(RemovalReason.DISCARDED);
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawn, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        setGovID(getRandom().nextInt(GovernmentData.MAX));
        setCustomName(GovernmentData.getName(getGovID()));
        return super.finalizeSpawn(world, difficulty, spawn, data, tag);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new LookAtPayerGoal());
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1, true));
        goalSelector.addGoal(3, new GiveLootGoal(this, 32, 1));
        goalSelector.addGoal(4, new FollowPlayersGoal(this, 1, 4, 16));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
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
    public InteractionResult interactAt(Player player, Vec3 hit, InteractionHand hand) {
        if (player instanceof ServerPlayer && hand == InteractionHand.MAIN_HAND) {
            NonNullList<ItemStack> tax = NonNullList.create();
            Debt debt = Debt.get(player);
            debt.getAllDebt().forEach(item -> {
                ItemStack stack = item.getDefaultInstance();
                stack.setCount(debt.getDebt(item));
                tax.add(stack);
            });
            if (!tax.isEmpty()) {
                ItemStack item = player.getItemInHand(hand);
                if (!item.isEmpty() && item.is(TagRegistry.BRIBE)) {
                    if (!player.isCreative()) {
                        item.shrink(1);
                    }
                    if (player.getRandom().nextDouble() < getBribeRate((ServerPlayer) player)) {
                        Debt.get(player).clear();
                        GovernmentData.multiplyShare(player.getServer(), getGovID(), player.getUUID(), 1.5);
                        level.broadcastEntityEvent(this, (byte) 0);
                    } else {
                        Debt.add(player, getTaxRate((ServerPlayer) player));
                        level.broadcastEntityEvent(this, (byte) 1);
                    }
                    return InteractionResult.CONSUME;
                } else {
                    setTradingPlayer(player);
                    Form1040.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new OpenTaxScreenPacket(tax, Debt.canPay(player), getUUID()));
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.interactAt(player, hit, hand);
    }

    @Nullable
    public Player getTradingPlayer() {
        return trading;
    }

    public void setTradingPlayer(@Nullable Player player) {
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
            remove(RemovalReason.DISCARDED);
        }
    }

    public double getTaxRate(ServerPlayer player) {
        double share = GovernmentData.getRelativeShare(player.getServer(), govID, player.getUUID());
        double base = ServerConfigs.INSTANCE.taxRate.get() * GovernmentRelations.get(player).getTaxFactor();
        return base / share;
    }

    public double getBribeRate(ServerPlayer player) {
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GovID", getGovID());
        tag.putBoolean("Payed", hasPayed());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("GovID", Tag.TAG_INT)) {
            setGovID(tag.getInt("GovID"));
        }
        if (tag.contains("Payed", Tag.TAG_BYTE)) {
            setPayed(tag.getBoolean("Payed"));
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean player) {
        super.dropCustomDeathLoot(source, looting, player);
        if (player && level instanceof ServerLevel) {
            List<ItemStack> items = GovernmentData.removeRandom((ServerLevel) level, getGovID(), getRandom(), ServerConfigs.INSTANCE.inventoryDropProportion.get());
            items.forEach(this::spawnAtLocation);
        }
    }

    @Override
    public boolean wasKilled(ServerLevel world, LivingEntity entity) {
        if (super.wasKilled(world, entity)) {
            if (entity instanceof Player) {
                GovernmentRelations relations = GovernmentRelations.get((Player) entity);
                relations.setTaxFactor(relations.getTaxFactor() * ServerConfigs.INSTANCE.taxIncreaseRate.get());
                GovernmentData.multiplyShare(world.getServer(), getGovID(), entity.getUUID(), 0.5);
            }
            return true;
        }
        return false;
    }

    private class LookAtPayerGoal extends LookAtPlayerGoal {

        public LookAtPayerGoal() {
            super(TaxCollectorEntity.this, Player.class, 8);
        }

        @Override
        public boolean canUse() {
            Player player = getTradingPlayer();
            if (player == null) {
                return false;
            } else {
                lookAt = player;
                return true;
            }
        }
    }

}
