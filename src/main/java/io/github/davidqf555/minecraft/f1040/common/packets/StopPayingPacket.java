package io.github.davidqf555.minecraft.f1040.common.packets;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.entities.TaxCollectorEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class StopPayingPacket {

    private static final BiConsumer<StopPayingPacket, PacketBuffer> ENCODER = (packet, buffer) -> {
        buffer.writeUUID(packet.collector);
    };
    private static final Function<PacketBuffer, StopPayingPacket> DECODER = buffer -> new StopPayingPacket(buffer.readUUID());
    private static final BiConsumer<StopPayingPacket, Supplier<NetworkEvent.Context>> CONSUMER = (packet, context) -> packet.handle(context.get());
    private final UUID collector;

    public StopPayingPacket(UUID collector) {
        this.collector = collector;
    }

    public static void register(int index) {
        Form1040.CHANNEL.registerMessage(index, StopPayingPacket.class, ENCODER, DECODER, CONSUMER);
    }

    private void handle(NetworkEvent.Context context) {
        if (context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ServerPlayerEntity player = context.getSender();
            context.enqueueWork(() -> {
                Entity collector = player.getLevel().getEntity(this.collector);
                if (collector instanceof TaxCollectorEntity) {
                    ((TaxCollectorEntity) collector).setTradingPlayer(null);
                }
            });
            context.setPacketHandled(true);
        }
    }
}
