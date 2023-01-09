package io.github.davidqf555.minecraft.f1040.common.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;

public class GovernmentRelations implements INBTSerializable<CompoundTag> {

    public static final Capability<GovernmentRelations> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    private double taxFactor = 1;

    public static GovernmentRelations get(Player player) {
        return player.getCapability(CAPABILITY).orElseThrow(NullPointerException::new);
    }

    public double getTaxFactor() {
        return taxFactor;
    }

    public void setTaxFactor(double taxFactor) {
        this.taxFactor = taxFactor;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("Tax", getTaxFactor());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("Tax", Tag.TAG_DOUBLE)) {
            setTaxFactor(nbt.getDouble("Tax"));
        }
    }

}
