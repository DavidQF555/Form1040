package io.github.davidqf555.minecraft.tax.client;

import io.github.davidqf555.minecraft.tax.client.render.TaxCollectorRenderer;
import io.github.davidqf555.minecraft.tax.common.RegistryHandler;
import io.github.davidqf555.minecraft.tax.common.Tax;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Tax.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class EventBusSubscriber {

    private EventBusSubscriber() {
    }

    @SubscribeEvent
    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(RegistryHandler.TAX_COLLECTOR_ENTITY.get(), TaxCollectorRenderer::new);
    }
}
