package io.github.davidqf555.minecraft.f1040.common.entities;

import io.github.davidqf555.minecraft.f1040.registration.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.BasicItemListing;

import javax.annotation.Nullable;

public class ShadyBankerEntity extends AbstractVillager {

    private int despawn;

    public ShadyBankerEntity(EntityType<? extends ShadyBankerEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zombie.class, 8.0F, 0.5D, 0.5D));
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Evoker.class, 12.0F, 0.5D, 0.5D));
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Vindicator.class, 8.0F, 0.5D, 0.5D));
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Vex.class, 8.0F, 0.5D, 0.5D));
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Pillager.class, 15.0F, 0.5D, 0.5D));
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Illusioner.class, 12.0F, 0.5D, 0.5D));
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zoglin.class, 10.0F, 0.5D, 0.5D));
        goalSelector.addGoal(1, new PanicGoal(this, 0.5D));
        goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        goalSelector.addGoal(2, new FollowPlayersGoal(this, 1, 4, 16));
        goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.35D));
        goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.35D));
        goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, 1.0F));
        goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel world, AgeableMob mate) {
        return null;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isAlive() && !isTrading()) {
            if (hand == InteractionHand.MAIN_HAND) {
                player.awardStat(Stats.TALKED_TO_VILLAGER);
            }
            if (!getOffers().isEmpty() && !level.isClientSide()) {
                setTradingPlayer(player);
                openTradingScreen(player, getDisplayName(), 1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
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
        VillagerTrades.ItemListing account = new BasicItemListing(getRandom().nextInt(5) + 8, ItemRegistry.OFFSHORE_BANK_ACCOUNT.get().getDefaultInstance(), 2, 10);
        getOffers().add(account.getOffer(this, getRandom()));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Despawn", despawn);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Despawn", Tag.TAG_INT)) {
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
            this.level.addFreshEntity(new ExperienceOrb(this.level, this.getX(), this.getY() + 0.5D, this.getZ(), i));
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
            remove(RemovalReason.DISCARDED);
        }
    }
}