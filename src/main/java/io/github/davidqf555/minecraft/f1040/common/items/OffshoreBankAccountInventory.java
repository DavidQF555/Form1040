package io.github.davidqf555.minecraft.f1040.common.items;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;


public class OffshoreBankAccountInventory extends Inventory implements INamedContainerProvider {

    public OffshoreBankAccountInventory() {
        super(5);
    }

    public static OffshoreBankAccountInventory get(ItemStack stack) {
        return stack.getCapability(Provider.capability).orElseGet(OffshoreBankAccountInventory::new);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent(Util.makeDescriptionId("container", new ResourceLocation(Form1040.MOD_ID, "offshore_bank_account")));
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        return new OffshoreBankAccountContainer(id, inventory, this);
    }

    public static class Provider implements ICapabilitySerializable<INBT> {

        @CapabilityInject(OffshoreBankAccountInventory.class)
        private static Capability<OffshoreBankAccountInventory> capability = null;
        private final LazyOptional<OffshoreBankAccountInventory> instance = LazyOptional.of(() -> Objects.requireNonNull(capability.getDefaultInstance()));

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return cap == capability ? instance.cast() : LazyOptional.empty();
        }

        @Override
        public INBT serializeNBT() {
            return capability.getStorage().writeNBT(capability, instance.orElseThrow(NullPointerException::new), null);
        }

        @Override
        public void deserializeNBT(INBT nbt) {
            capability.getStorage().readNBT(capability, instance.orElseThrow(NullPointerException::new), null, nbt);
        }
    }

    public static class Storage implements Capability.IStorage<OffshoreBankAccountInventory> {

        @Nullable
        @Override
        public INBT writeNBT(Capability<OffshoreBankAccountInventory> capability, OffshoreBankAccountInventory instance, Direction side) {
            return instance.createTag();
        }

        @Override
        public void readNBT(Capability<OffshoreBankAccountInventory> capability, OffshoreBankAccountInventory instance, Direction side, INBT nbt) {
            instance.fromTag((ListNBT) nbt);
        }
    }
}