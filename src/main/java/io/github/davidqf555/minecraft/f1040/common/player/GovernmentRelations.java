package io.github.davidqf555.minecraft.f1040.common.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

public class GovernmentRelations implements INBTSerializable<CompoundNBT> {

    @CapabilityInject(GovernmentRelations.class)
    public static Capability<GovernmentRelations> capability = null;
    private double taxFactor = 1;

    public static GovernmentRelations get(PlayerEntity player) {
        return player.getCapability(capability).orElseThrow(NullPointerException::new);
    }

    public double getTaxFactor() {
        return taxFactor;
    }

    public void setTaxFactor(double taxFactor) {
        this.taxFactor = taxFactor;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putDouble("Tax", getTaxFactor());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("Tax", Constants.NBT.TAG_DOUBLE)) {
            setTaxFactor(nbt.getDouble("Tax"));
        }
    }

}
