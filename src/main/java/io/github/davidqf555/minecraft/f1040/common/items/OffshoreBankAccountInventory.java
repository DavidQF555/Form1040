package io.github.davidqf555.minecraft.f1040.common.items;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class OffshoreBankAccountInventory extends SimpleContainer implements MenuProvider {

    public OffshoreBankAccountInventory() {
        super(5);
    }

    public static OffshoreBankAccountInventory get(ItemStack stack) {
        return stack.getCapability(Provider.CAPABILITY).orElseGet(OffshoreBankAccountInventory::new);
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent(Util.makeDescriptionId("container", new ResourceLocation(Form1040.MOD_ID, "offshore_bank_account")));
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new OffshoreBankAccountContainer(id, inventory, this);
    }

    public static class Provider implements ICapabilitySerializable<ListTag> {

        private static final Capability<OffshoreBankAccountInventory> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
        });
        private final LazyOptional<OffshoreBankAccountInventory> instance = LazyOptional.of(OffshoreBankAccountInventory::new);

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return cap == CAPABILITY ? instance.cast() : LazyOptional.empty();
        }

        @Override
        public ListTag serializeNBT() {
            return instance.orElseThrow(NullPointerException::new).createTag();
        }

        @Override
        public void deserializeNBT(ListTag nbt) {
            instance.orElseThrow(NullPointerException::new).fromTag(nbt);
        }
    }

}