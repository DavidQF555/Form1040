package io.github.davidqf555.minecraft.f1040.common.packets;

import io.github.davidqf555.minecraft.f1040.client.gui.TaxScreen;
import io.github.davidqf555.minecraft.f1040.common.Form1040;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class OpenTaxScreenPacket {

    private static final BiConsumer<OpenTaxScreenPacket, PacketBuffer> ENCODER = (packet, buffer) -> {
        buffer.writeInt(packet.items.size());
        packet.items.forEach(buffer::writeItem);
        buffer.writeBoolean(packet.canPay);
        buffer.writeUUID(packet.collector);
    };
    private static final Function<PacketBuffer, OpenTaxScreenPacket> DECODER = buffer -> {
        int size = buffer.readInt();
        NonNullList<ItemStack> items = NonNullList.create();
        for (int i = 0; i < size; i++) {
            items.add(buffer.readItem());
        }
        return new OpenTaxScreenPacket(items, buffer.readBoolean(), buffer.readUUID());
    };
    private static final BiConsumer<OpenTaxScreenPacket, Supplier<NetworkEvent.Context>> CONSUMER = (packet, context) -> packet.handle(context.get());
    private final NonNullList<ItemStack> items;
    private final boolean canPay;
    private final UUID collector;

    public OpenTaxScreenPacket(NonNullList<ItemStack> items, boolean canPay, UUID collector) {
        this.items = items;
        this.canPay = canPay;
        this.collector = collector;
    }

    public static void register(int index) {
        Form1040.CHANNEL.registerMessage(index, OpenTaxScreenPacket.class, ENCODER, DECODER, CONSUMER);
    }

    private void handle(NetworkEvent.Context context) {
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.enqueueWork(() -> Minecraft.getInstance().setScreen(new TaxScreen(items, canPay, collector)));
            context.setPacketHandled(true);
        }
    }
}
