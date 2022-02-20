package io.github.davidqf555.minecraft.f1040.common;

import io.github.davidqf555.minecraft.f1040.common.entities.TaxCollectorEntity;
import io.github.davidqf555.minecraft.f1040.common.packets.OpenTaxScreenPacket;
import io.github.davidqf555.minecraft.f1040.common.packets.PayTaxesPacket;
import io.github.davidqf555.minecraft.f1040.common.packets.StopPayingPacket;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                Debt.get(event.getPlayer()).deserializeNBT(Debt.get(event.getOriginal()).serializeNBT());
            }
        }

        @SubscribeEvent
        public static void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player) {
                Debt backend = new Debt();
                LazyOptional<Debt> storage = LazyOptional.of(() -> backend);
                ICapabilityProvider provider = new ICapabilitySerializable<CompoundTag>() {
                    @NotNull
                    @Override
                    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                        return cap == Debt.CAPABILITY ? storage.cast() : LazyOptional.empty();
                    }

                    @Override
                    public CompoundTag serializeNBT() {
                        return backend.serializeNBT();
                    }

                    @Override
                    public void deserializeNBT(CompoundTag nbt) {
                        backend.deserializeNBT(nbt);
                    }
                };
                event.addCapability(DEBT, provider);
            }
        }

        @SubscribeEvent
        public static void onWorldTick(TickEvent.WorldTickEvent event) {
            if (event.phase == TickEvent.Phase.END && event.world instanceof ServerLevel && !event.world.dimensionType().hasFixedTime() && event.world.getDayTime() % ServerConfigs.INSTANCE.taxPeriod.get() == 0) {
                event.world.players().forEach(player -> {
                    if (!player.isCreative() && !player.isSpectator()) {
                        Debt.add(player);
                        int size = Debt.get(player).getAllDebt().size();
                        for (int i = 0; i < size / 3 + 1; i++) {
                            if (event.world.getRandom().nextDouble() < ServerConfigs.INSTANCE.taxCollectorRate.get()) {
                                TaxCollectorEntity.spawnNear(player, 8);
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
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.register(Debt.class);
        }

        @SubscribeEvent
        public static void onFMLCommonSetup(FMLCommonSetupEvent event) {
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
