package io.github.davidqf555.minecraft.tax.common.packets;

import io.github.davidqf555.minecraft.tax.common.Debt;
import io.github.davidqf555.minecraft.tax.common.Tax;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PayTaxesPacket {

    private static final BiConsumer<PayTaxesPacket, PacketBuffer> ENCODER = (packet, buffer) -> {
    };
    private static final Function<PacketBuffer, PayTaxesPacket> DECODER = buffer -> new PayTaxesPacket();
    private static final BiConsumer<PayTaxesPacket, Supplier<NetworkEvent.Context>> CONSUMER = (packet, context) -> packet.handle(context.get());

    public static void register(int index) {
        Tax.CHANNEL.registerMessage(index, PayTaxesPacket.class, ENCODER, DECODER, CONSUMER);
    }

    private void handle(NetworkEvent.Context context) {
        if (context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ServerPlayerEntity player = context.getSender();
            context.enqueueWork(() -> {
                if (Debt.canPay(player)) {
                    Debt.pay(player);
                }
            });
            context.setPacketHandled(true);
        }
    }
}
