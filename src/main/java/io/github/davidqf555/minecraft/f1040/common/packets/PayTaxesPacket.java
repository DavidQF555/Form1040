package io.github.davidqf555.minecraft.f1040.common.packets;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.entities.TaxCollectorEntity;
import io.github.davidqf555.minecraft.f1040.common.player.Debt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PayTaxesPacket {

    private static final BiConsumer<PayTaxesPacket, PacketBuffer> ENCODER = (packet, buffer) -> buffer.writeUUID(packet.collector);
    private static final Function<PacketBuffer, PayTaxesPacket> DECODER = buffer -> new PayTaxesPacket(buffer.readUUID());
    private static final BiConsumer<PayTaxesPacket, Supplier<NetworkEvent.Context>> CONSUMER = (packet, context) -> packet.handle(context.get());
    private final UUID collector;

    public PayTaxesPacket(UUID collector) {
        this.collector = collector;
    }

    public static void register(int index) {
        Form1040.CHANNEL.registerMessage(index, PayTaxesPacket.class, ENCODER, DECODER, CONSUMER, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    private void handle(NetworkEvent.Context context) {
        ServerPlayerEntity player = context.getSender();
        context.enqueueWork(() -> {
            Entity entity = player.getLevel().getEntity(collector);
            if (entity instanceof TaxCollectorEntity && player.equals(((TaxCollectorEntity) entity).getTradingPlayer()) && Debt.canPay(player)) {
                Debt.pay(player, ((TaxCollectorEntity) entity).getGovID());
            }
        });
        context.setPacketHandled(true);
    }
}
