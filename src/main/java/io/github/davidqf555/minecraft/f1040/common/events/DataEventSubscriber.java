package io.github.davidqf555.minecraft.f1040.common.events;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.ServerConfigs;
import io.github.davidqf555.minecraft.f1040.common.player.Debt;
import io.github.davidqf555.minecraft.f1040.common.player.GovernmentRelations;
import io.github.davidqf555.minecraft.f1040.common.player.NBTCapabilityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
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
            ServerPlayerEntity original = (ServerPlayerEntity) event.getOriginal();
            ServerPlayerEntity resp = (ServerPlayerEntity) event.getPlayer();
            Debt.get(resp).deserializeNBT(Debt.get(original).serializeNBT());
        }
    }

    @SubscribeEvent
    public static void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            event.addCapability(DEBT, new NBTCapabilityProvider<>(Debt.capability, new Debt()));
            event.addCapability(GOVERNMENT_RELATIONS, new NBTCapabilityProvider<>(GovernmentRelations.capability, new GovernmentRelations()));
        }
    }

}
