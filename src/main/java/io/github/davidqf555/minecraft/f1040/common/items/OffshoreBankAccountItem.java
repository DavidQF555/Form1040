package io.github.davidqf555.minecraft.f1040.common.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public class OffshoreBankAccountItem extends Item {

    public OffshoreBankAccountItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide()) {
            return ActionResult.success(stack);
        }
        player.openMenu(OffshoreBankAccountInventory.get(stack));
        return ActionResult.consume(stack);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new OffshoreBankAccountInventory.Provider();
    }

}
