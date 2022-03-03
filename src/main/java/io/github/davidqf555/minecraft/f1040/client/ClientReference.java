package io.github.davidqf555.minecraft.f1040.client;

import io.github.davidqf555.minecraft.f1040.client.gui.TaxScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public final class ClientReference {

    private ClientReference() {
    }

    public static void openTaxScreen(NonNullList<ItemStack> items, boolean canPay, UUID collector) {
        Minecraft.getInstance().setScreen(new TaxScreen(items, canPay, collector));
    }
}
