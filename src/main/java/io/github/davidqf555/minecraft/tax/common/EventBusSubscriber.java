package io.github.davidqf555.minecraft.tax.common;

import io.github.davidqf555.minecraft.tax.common.entities.TaxCollectorEntity;
import io.github.davidqf555.minecraft.tax.common.packets.OpenTaxScreenPacket;
import io.github.davidqf555.minecraft.tax.common.packets.PayTaxesPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public final class EventBusSubscriber {

    private EventBusSubscriber() {
    }

    @Mod.EventBusSubscriber(modid = Tax.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeBus {

        private static final ResourceLocation DEBT = new ResourceLocation(Tax.MOD_ID, "debt");

        private ForgeBus() {
        }

        @SubscribeEvent
        public static void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof PlayerEntity) {
                event.addCapability(DEBT, new Debt.Provider());
            }
        }

        @SubscribeEvent
        public static void onWorldTick(TickEvent.WorldTickEvent event) {
            if (event.phase == TickEvent.Phase.END && event.world instanceof ServerWorld && !event.world.dimensionType().hasFixedTime() && event.world.getDayTime() % 24000 == 0) {
                event.world.players().forEach(Debt::add);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = Tax.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {

        private ModBus() {
        }

        @SubscribeEvent
        public static void onFMLCommonSetup(FMLCommonSetupEvent event) {
            CapabilityManager.INSTANCE.register(Debt.class, new Debt.Storage(), Debt::new);
            event.enqueueWork(() -> {
                OpenTaxScreenPacket.register(0);
                PayTaxesPacket.register(1);
            });
        }

        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(RegistryHandler.TAX_COLLECTOR_ENTITY.get(), TaxCollectorEntity.createMobAttributes().build());
        }

    }
}
