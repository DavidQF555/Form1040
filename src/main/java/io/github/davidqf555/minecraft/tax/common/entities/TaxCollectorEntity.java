package io.github.davidqf555.minecraft.tax.common.entities;

import io.github.davidqf555.minecraft.tax.common.Debt;
import io.github.davidqf555.minecraft.tax.common.RegistryHandler;
import io.github.davidqf555.minecraft.tax.common.ServerConfigs;
import io.github.davidqf555.minecraft.tax.common.Tax;
import io.github.davidqf555.minecraft.tax.common.packets.OpenTaxScreenPacket;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

    public TaxCollectorEntity(World world) {
        super(RegistryHandler.TAX_COLLECTOR_ENTITY.get(), world);
        setItemInHand(Hand.MAIN_HAND, Items.IRON_AXE.getDefaultInstance());
    }

    public static AttributeModifierMap.MutableAttribute createMobAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35F);
    }

    public static void spawnNear(PlayerEntity player, int range) {
        BlockPos center = player.blockPosition();
        Random random = player.getRandom();
        EntityType<TaxCollectorEntity> type = RegistryHandler.TAX_COLLECTOR_ENTITY.get();
        for (int i = 0; i < 10; i++) {
            int j = center.getX() + random.nextInt(range * 2) - range;
            int k = center.getZ() + random.nextInt(range * 2) - range;
            int l = player.level.getHeight(Heightmap.Type.WORLD_SURFACE, j, k);
            BlockPos pos = new BlockPos(j, l, k);
            if (WorldEntitySpawner.isSpawnPositionOk(EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, player.level, pos, type)) {
                TaxCollectorEntity entity = type.create(player.level);
                if (entity != null) {
                    Vector3d vec = Vector3d.atBottomCenterOf(pos);
                    entity.setPos(vec.x(), vec.y(), vec.z());
                    player.level.addFreshEntity(entity);
                    return;
                }
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new LookAtPayerGoal());
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1, true));
        this.goalSelector.addGoal(3, new LookAtGoal(this, PlayerEntity.class, 6));
        this.goalSelector.addGoal(4, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 10, true, false, player -> Debt.get((PlayerEntity) player).getAllDebt().size() >= ServerConfigs.INSTANCE.punishAmt.get()));
    }

    @Override
    public ActionResultType interactAt(PlayerEntity player, Vector3d hit, Hand hand) {
        if (player instanceof ServerPlayerEntity) {
            NonNullList<ItemStack> tax = NonNullList.create();
            Debt debt = Debt.get(player);
            debt.getAllDebt().forEach(item -> {
                ItemStack stack = item.getDefaultInstance();
                stack.setCount(debt.getDebt(item));
                tax.add(stack);
            });
            if (!tax.isEmpty()) {
                setTradingPlayer(player);
                Tax.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new OpenTaxScreenPacket(tax, Debt.canPay(player), getUUID()));
                return ActionResultType.SUCCESS;
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

    public static class Factory implements EntityType.IFactory<TaxCollectorEntity> {

        @Override
        public TaxCollectorEntity create(EntityType<TaxCollectorEntity> type, World world) {
            return new TaxCollectorEntity(world);
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
