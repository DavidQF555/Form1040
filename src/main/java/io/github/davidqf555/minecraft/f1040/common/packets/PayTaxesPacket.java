package io.github.davidqf555.minecraft.f1040.common.packets;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.player.Debt;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PayTaxesPacket {

    private static final BiConsumer<PayTaxesPacket, FriendlyByteBuf> ENCODER = (packet, buffer) -> {
    };
    private static final Function<FriendlyByteBuf, PayTaxesPacket> DECODER = buffer -> new PayTaxesPacket();
    private static final BiConsumer<PayTaxesPacket, Supplier<NetworkEvent.Context>> CONSUMER = (packet, context) -> packet.handle(context.get());

    public static void register(int index) {
        Form1040.CHANNEL.registerMessage(index, PayTaxesPacket.class, ENCODER, DECODER, CONSUMER, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    private void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        context.enqueueWork(() -> {
            if (Debt.canPay(player)) {
                Debt.pay(player);
            }
        });
        context.setPacketHandled(true);
    }
}
