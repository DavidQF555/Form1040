package io.github.davidqf555.minecraft.f1040.common.entities;

import io.github.davidqf555.minecraft.f1040.registration.ItemRegistry;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.BasicTrade;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class ShadyBankerEntity extends AbstractVillagerEntity {

    private int despawn;

    public ShadyBankerEntity(EntityType<? extends ShadyBankerEntity> type, World world) {
        super(type, world);
        forcedLoading = true;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new SwimGoal(this));
        goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, ZombieEntity.class, 8, 0.5, 0.5));
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, EvokerEntity.class, 12, 0.5, 0.5));
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, VindicatorEntity.class, 8, 0.5, 0.5));
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, VexEntity.class, 8, 0.5, 0.5));
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, PillagerEntity.class, 15, 0.5, 0.5));
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, IllusionerEntity.class, 12, 0.5, 0.5));
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, ZoglinEntity.class, 10, 0.5, 0.5));
        goalSelector.addGoal(1, new PanicGoal(this, 0.5));
        goalSelector.addGoal(1, new LookAtCustomerGoal(this));
        goalSelector.addGoal(2, new FollowPlayersGoal(this, 1, 4, 16));
        goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 0.35));
        goalSelector.addGoal(4, new WaterAvoidingRandomWalkingGoal(this, 0.35));
        goalSelector.addGoal(5, new LookAtWithoutMovingGoal(this, PlayerEntity.class, 3, 1));
        goalSelector.addGoal(6, new LookAtGoal(this, MobEntity.class, 8));
    }

    @Override
    @Nullable
    public AgeableEntity getBreedOffspring(ServerWorld world, AgeableEntity mate) {
        return null;
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        if (isAlive() && !isTrading()) {
            if (hand == Hand.MAIN_HAND) {
                player.awardStat(Stats.TALKED_TO_VILLAGER);
            }
            if (!getOffers().isEmpty() && !level.isClientSide()) {
                setTradingPlayer(player);
                openTradingScreen(player, getDisplayName(), 1);
            }
            return ActionResultType.sidedSuccess(level.isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    public void setDespawn(int despawn) {
        this.despawn = despawn;
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    protected void updateTrades() {
        VillagerTrades.ITrade account = new BasicTrade(getRandom().nextInt(5) + 8, ItemRegistry.OFFSHORE_BANK_ACCOUNT.get().getDefaultInstance(), 2, 10);
        getOffers().add(account.getOffer(this, getRandom()));
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Despawn", despawn);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Despawn", Constants.NBT.TAG_INT)) {
            despawn = tag.getInt("Despawn");
        }
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        if (offer.shouldRewardExp()) {
            int i = 3 + this.random.nextInt(4);
            this.level.addFreshEntity(new ExperienceOrbEntity(this.level, this.getX(), this.getY() + 0.5D, this.getZ(), i));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isTrading() ? SoundEvents.WANDERING_TRADER_TRADE : SoundEvents.WANDERING_TRADER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
        return SoundEvents.WANDERING_TRADER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WANDERING_TRADER_DEATH;
    }

    @Override
    protected SoundEvent getDrinkingSound(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.MILK_BUCKET ? SoundEvents.WANDERING_TRADER_DRINK_MILK : SoundEvents.WANDERING_TRADER_DRINK_POTION;
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean yes) {
        return yes ? SoundEvents.WANDERING_TRADER_YES : SoundEvents.WANDERING_TRADER_NO;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.WANDERING_TRADER_YES;
    }

    @Override
    protected void customServerAiStep() {
        if (despawn > 0 && !this.isTrading() && --despawn == 0) {
            remove();
        }
    }
}
