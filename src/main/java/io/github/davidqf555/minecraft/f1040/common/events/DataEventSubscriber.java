package io.github.davidqf555.minecraft.f1040.common.events;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.ServerConfigs;
import io.github.davidqf555.minecraft.f1040.common.items.OffshoreBankAccountInventory;
import io.github.davidqf555.minecraft.f1040.common.player.Debt;
import io.github.davidqf555.minecraft.f1040.common.player.GovernmentRelations;
import io.github.davidqf555.minecraft.f1040.common.player.NBTCapabilityProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Form1040.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DataEventSubscriber {

    private static final ResourceLocation DEBT = new ResourceLocation(Form1040.MOD_ID, "debt");
    private static final ResourceLocation GOVERNMENT_RELATIONS = new ResourceLocation(Form1040.MOD_ID, "government_relations");

    private DataEventSubscriber() {
    }

    @SubscribeEvent
    public static void onClonePlayerEvent(PlayerEvent.Clone event) {
        if (ServerConfigs.INSTANCE.persistent.get() && event.isWasDeath()) {
            ServerPlayer original = (ServerPlayer) event.getOriginal();
            ServerPlayer resp = (ServerPlayer) event.getEntity();
            Debt.get(resp).deserializeNBT(Debt.get(original).serializeNBT());
        }
    }

    @SubscribeEvent
    public static void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(DEBT, new NBTCapabilityProvider<>(Debt.CAPABILITY, new Debt()));
            event.addCapability(GOVERNMENT_RELATIONS, new NBTCapabilityProvider<>(GovernmentRelations.CAPABILITY, new GovernmentRelations()));
        }
    }

    @Mod.EventBusSubscriber(modid = Form1040.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {

        private ModBus() {
        }

        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.register(Debt.class);
            event.register(OffshoreBankAccountInventory.class);
            event.register(GovernmentRelations.class);
        }

    }

}
