package io.github.davidqf555.minecraft.f1040.client;

import io.github.davidqf555.minecraft.f1040.client.gui.OffshoreBankAccountScreen;
import io.github.davidqf555.minecraft.f1040.client.model.MixedVillagerModel;
import io.github.davidqf555.minecraft.f1040.client.render.ShadyBankerRenderer;
import io.github.davidqf555.minecraft.f1040.client.render.TaxCollectorRenderer;
import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.registration.ContainerRegistry;
import io.github.davidqf555.minecraft.f1040.registration.EntityRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Form1040.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class EventBusSubscriber {

    private EventBusSubscriber() {
    }

    @SubscribeEvent
    public static void onRegisterLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MixedVillagerModel.MIXED_VILLAGER, MixedVillagerModel::createBodyLayer);
        event.registerLayerDefinition(MixedVillagerModel.EXTENDED_VILLAGER, MixedVillagerModel::createExtendedVillager);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.TAX_COLLECTOR.get(), TaxCollectorRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SHADY_BANKER.get(), ShadyBankerRenderer::new);
    }

    @SubscribeEvent
    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ContainerRegistry.OFFSHORE_BANK_ACCOUNT.get(), OffshoreBankAccountScreen::new);
        });
    }

}
