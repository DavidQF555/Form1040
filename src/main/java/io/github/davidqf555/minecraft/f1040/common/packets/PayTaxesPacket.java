package io.github.davidqf555.minecraft.f1040.common.packets;

import io.github.davidqf555.minecraft.f1040.common.Debt;
import io.github.davidqf555.minecraft.f1040.common.Form1040;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PayTaxesPacket {

    private static final BiConsumer<PayTaxesPacket, PacketBuffer> ENCODER = (packet, buffer) -> {
    };
    private static final Function<PacketBuffer, PayTaxesPacket> DECODER = buffer -> new PayTaxesPacket();
    private static final BiConsumer<PayTaxesPacket, Supplier<NetworkEvent.Context>> CONSUMER = (packet, context) -> packet.handle(context.get());

    public static void register(int index) {
        Form1040.CHANNEL.registerMessage(index, PayTaxesPacket.class, ENCODER, DECODER, CONSUMER, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    private void handle(NetworkEvent.Context context) {
        ServerPlayerEntity player = context.getSender();
        context.enqueueWork(() -> {
            if (Debt.canPay(player)) {
                Debt.pay(player);
            }
        });
        context.setPacketHandled(true);
    }
}
