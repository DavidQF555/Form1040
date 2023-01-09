package io.github.davidqf555.minecraft.f1040.common.player;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NBTCapabilityProvider<T extends INBT, M extends INBTSerializable<T>> implements ICapabilitySerializable<T> {

    private final Capability<M> capability;
    private final M instance;

    public NBTCapabilityProvider(Capability<M> capability, M instance) {
        this.capability = capability;
        this.instance = instance;
    }

    @Nonnull
    @Override
    public <Y> LazyOptional<Y> getCapability(@Nonnull Capability<Y> cap, @Nullable Direction side) {
        return cap == capability ? LazyOptional.of(() -> instance).cast() : LazyOptional.empty();
    }

    @Override
    public T serializeNBT() {
        return instance.serializeNBT();
    }

    @Override
    public void deserializeNBT(T nbt) {
        instance.deserializeNBT(nbt);
    }

}
