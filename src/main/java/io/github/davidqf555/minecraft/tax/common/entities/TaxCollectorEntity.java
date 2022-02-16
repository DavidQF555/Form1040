package io.github.davidqf555.minecraft.tax.common.entities;

import io.github.davidqf555.minecraft.tax.common.Debt;
import io.github.davidqf555.minecraft.tax.common.RegistryHandler;
import io.github.davidqf555.minecraft.tax.common.Tax;
import io.github.davidqf555.minecraft.tax.common.packets.OpenTaxScreenPacket;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.INPC;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class TaxCollectorEntity extends CreatureEntity implements INPC {

    public TaxCollectorEntity(World world) {
        super(RegistryHandler.TAX_COLLECTOR_ENTITY.get(), world);
    }

    public static AttributeModifierMap.MutableAttribute createMobAttributes() {
        return MobEntity.createMobAttributes();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9D, 32.0F));
        this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 10, true, false, player -> Debt.get((PlayerEntity) player).getAllDebt().size() > 10));
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
                Tax.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new OpenTaxScreenPacket(tax, Debt.canPay(player)));
                return ActionResultType.SUCCESS;
            }
        }
        return super.interactAt(player, hit, hand);
    }

    public static class Factory implements EntityType.IFactory<TaxCollectorEntity> {

        @Override
        public TaxCollectorEntity create(EntityType<TaxCollectorEntity> type, World world) {
            return new TaxCollectorEntity(world);
        }
    }
}
