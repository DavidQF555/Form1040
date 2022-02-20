package io.github.davidqf555.minecraft.f1040.common.entities;

import io.github.davidqf555.minecraft.f1040.common.Debt;
import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.RegistryHandler;
import io.github.davidqf555.minecraft.f1040.common.ServerConfigs;
import io.github.davidqf555.minecraft.f1040.common.packets.OpenTaxScreenPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Random;

public class TaxCollectorEntity extends PathfinderMob implements Npc {

    private Player trading;

    public TaxCollectorEntity(Level world) {
        super(RegistryHandler.TAX_COLLECTOR_ENTITY.get(), world);
        setItemInHand(InteractionHand.MAIN_HAND, Items.IRON_AXE.getDefaultInstance());
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35F);
    }

    public static void spawnNear(Player player, int range) {
        BlockPos center = player.blockPosition();
        Random random = player.getRandom();
        EntityType<TaxCollectorEntity> type = RegistryHandler.TAX_COLLECTOR_ENTITY.get();
        for (int i = 0; i < 10; i++) {
            int j = center.getX() + random.nextInt(range * 2) - range;
            int k = center.getZ() + random.nextInt(range * 2) - range;
            int l = player.level.getHeight(Heightmap.Types.WORLD_SURFACE, j, k);
            BlockPos pos = new BlockPos(j, l, k);
            if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, player.level, pos, type)) {
                TaxCollectorEntity entity = type.create(player.level);
                if (entity != null) {
                    Vec3 vec = Vec3.atBottomCenterOf(pos);
                    entity.setPos(vec.x(), vec.y(), vec.z());
                    player.level.addFreshEntity(entity);
                    return;
                }
            }
        }
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new LookAtPayerGoal());
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1, true));
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        targetSelector.addGoal(0, new HurtByTargetGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, player -> Debt.get((Player) player).getAllDebt().size() >= ServerConfigs.INSTANCE.punishAmt.get()));
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hit, InteractionHand hand) {
        if (player instanceof ServerPlayer) {
            NonNullList<ItemStack> tax = NonNullList.create();
            Debt debt = Debt.get(player);
            debt.getAllDebt().forEach(item -> {
                ItemStack stack = item.getDefaultInstance();
                stack.setCount(debt.getDebt(item));
                tax.add(stack);
            });
            if (!tax.isEmpty()) {
                setTradingPlayer(player);
                Form1040.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new OpenTaxScreenPacket(tax, Debt.canPay(player), getUUID()));
                return InteractionResult.SUCCESS;
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

    @Override
    public void aiStep() {
        super.aiStep();
        if (tickCount >= ServerConfigs.INSTANCE.taxPeriod.get()) {
            remove(RemovalReason.DISCARDED);
        }
    }

    public static class Factory implements EntityType.EntityFactory<TaxCollectorEntity> {

        @Override
        public TaxCollectorEntity create(EntityType<TaxCollectorEntity> type, Level world) {
            return new TaxCollectorEntity(world);
        }
    }

    private class LookAtPayerGoal extends LookAtPlayerGoal {

        public LookAtPayerGoal() {
            super(TaxCollectorEntity.this, Player.class, 8);
        }

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
