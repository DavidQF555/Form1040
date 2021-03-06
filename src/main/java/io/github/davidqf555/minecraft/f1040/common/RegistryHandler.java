package io.github.davidqf555.minecraft.f1040.common;

import io.github.davidqf555.minecraft.f1040.common.entities.TaxCollectorEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Form1040.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RegistryHandler {

    public static final RegistryObject<EntityType<TaxCollectorEntity>> TAX_COLLECTOR_ENTITY = RegistryObject.of(new ResourceLocation(Form1040.MOD_ID, "tax_collector"), ForgeRegistries.ENTITIES);

    private RegistryHandler() {
    }

    @SubscribeEvent
    public static void onRegisterEntityTypes(RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().register(EntityType.Builder.of(new TaxCollectorEntity.Factory(), MobCategory.MISC).sized(0.6f, 1.95f).build(TAX_COLLECTOR_ENTITY.getId().getPath()).setRegistryName(TAX_COLLECTOR_ENTITY.getId()));
    }

}
