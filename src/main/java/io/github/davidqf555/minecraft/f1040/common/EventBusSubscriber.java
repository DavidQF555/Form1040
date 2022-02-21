package io.github.davidqf555.minecraft.f1040.common;

import io.github.davidqf555.minecraft.f1040.common.entities.TargetIndebtedGoal;
import io.github.davidqf555.minecraft.f1040.common.entities.TaxCollectorEntity;
import io.github.davidqf555.minecraft.f1040.common.packets.OpenTaxScreenPacket;
import io.github.davidqf555.minecraft.f1040.common.packets.PayTaxesPacket;
import io.github.davidqf555.minecraft.f1040.common.packets.StopPayingPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public final class EventBusSubscriber {

    private EventBusSubscriber() {
    }

    @Mod.EventBusSubscriber(modid = Form1040.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeBus {

        private static final ResourceLocation DEBT = new ResourceLocation(Form1040.MOD_ID, "debt");

        private ForgeBus() {
        }

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            TaxCommand.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void onClonePlayerEvent(PlayerEvent.Clone event) {
            if (ServerConfigs.INSTANCE.persistent.get() && event.isWasDeath()) {
                ServerPlayerEntity original = (ServerPlayerEntity) event.getOriginal();
                ServerPlayerEntity resp = (ServerPlayerEntity) event.getPlayer();
                Debt.get(resp).deserializeNBT(Debt.get(original).serializeNBT());
            }
        }

        @SubscribeEvent
        public static void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof PlayerEntity) {
                event.addCapability(DEBT, new Debt.Provider());
            }
        }

        @SubscribeEvent
        public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
            Entity entity = event.getEntity();
            if (entity instanceof IronGolemEntity) {
                ((IronGolemEntity) entity).targetSelector.addGoal(3, new TargetIndebtedGoal<>((MobEntity) entity, true));
            }
        }

        @SubscribeEvent
        public static void onWorldTick(TickEvent.WorldTickEvent event) {
            if (event.phase == TickEvent.Phase.END && event.world instanceof ServerWorld && !event.world.dimensionType().hasFixedTime() && event.world.getDayTime() % ServerConfigs.INSTANCE.taxPeriod.get() == 0) {
                event.world.players().forEach(player -> {
                    if (!player.isCreative() && !player.isSpectator()) {
                        if (ServerConfigs.INSTANCE.villageRange.get() == -1 || ((ServerWorld) event.world).sectionsToVillage(SectionPos.of(player)) <= SectionPos.blockToSection(ServerConfigs.INSTANCE.villageRange.get())) {
                            Debt.add(player);
                            int min = ServerConfigs.INSTANCE.taxCollectorMin.get();
                            int max = ServerConfigs.INSTANCE.taxCollectorMax.get();
                            TaxCollectorEntity.spawn(player, RegistryHandler.TAX_COLLECTOR_ENTITY.get(), min, max);
                            if (Debt.isIndebted(player)) {
                                for (int i = 0; i < ServerConfigs.INSTANCE.ironGolemCount.get(); i++) {
                                    TaxCollectorEntity.spawn(player, EntityType.IRON_GOLEM, min, max);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    @Mod.EventBusSubscriber(modid = Form1040.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {

        private ModBus() {
        }

        @SubscribeEvent
        public static void onFMLCommonSetup(FMLCommonSetupEvent event) {
            CapabilityManager.INSTANCE.register(Debt.class, new Debt.Storage(), Debt::new);
            event.enqueueWork(() -> {
                OpenTaxScreenPacket.register(0);
                PayTaxesPacket.register(1);
                StopPayingPacket.register(2);
            });
        }

        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(RegistryHandler.TAX_COLLECTOR_ENTITY.get(), TaxCollectorEntity.createMobAttributes().build());
        }

    }
}
