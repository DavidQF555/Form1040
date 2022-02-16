package io.github.davidqf555.minecraft.tax.common.packets;

import io.github.davidqf555.minecraft.tax.client.gui.TaxScreen;
import io.github.davidqf555.minecraft.tax.common.Tax;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class OpenTaxScreenPacket {

    private static final BiConsumer<OpenTaxScreenPacket, PacketBuffer> ENCODER = (packet, buffer) -> {
        buffer.writeInt(packet.items.size());
        packet.items.forEach(buffer::writeItem);
        buffer.writeBoolean(packet.canPay);
    };
    private static final Function<PacketBuffer, OpenTaxScreenPacket> DECODER = buffer -> {
        int size = buffer.readInt();
        NonNullList<ItemStack> items = NonNullList.create();
        for (int i = 0; i < size; i++) {
            items.add(buffer.readItem());
        }
        return new OpenTaxScreenPacket(items, buffer.readBoolean());
    };
    private static final BiConsumer<OpenTaxScreenPacket, Supplier<NetworkEvent.Context>> CONSUMER = (packet, context) -> packet.handle(context.get());
    private final NonNullList<ItemStack> items;
    private final boolean canPay;

    public OpenTaxScreenPacket(NonNullList<ItemStack> items, boolean canPay) {
        this.items = items;
        this.canPay = canPay;
    }

    public static void register(int index) {
        Tax.CHANNEL.registerMessage(index, OpenTaxScreenPacket.class, ENCODER, DECODER, CONSUMER);
    }

    private void handle(NetworkEvent.Context context) {
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.enqueueWork(() -> Minecraft.getInstance().setScreen(new TaxScreen(items, canPay)));
            context.setPacketHandled(true);
        }
    }
}
