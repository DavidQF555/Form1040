package io.github.davidqf555.minecraft.f1040.client;

import io.github.davidqf555.minecraft.f1040.client.gui.OffshoreBankAccountScreen;
import io.github.davidqf555.minecraft.f1040.client.render.TaxCollectorRenderer;
import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.registration.ContainerRegistry;
import io.github.davidqf555.minecraft.f1040.registration.EntityRegistry;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Form1040.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class EventBusSubscriber {

    private EventBusSubscriber() {
    }

    @SubscribeEvent
    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.TAX_COLLECTOR.get(), TaxCollectorRenderer::new);
        event.enqueueWork(() -> {
            ScreenManager.register(ContainerRegistry.OFFSHORE_BANK_ACCOUNT.get(), OffshoreBankAccountScreen::new);
        });
    }

}
