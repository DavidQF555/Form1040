package io.github.davidqf555.minecraft.f1040.registration;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.entities.TaxCollectorEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Form1040.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class EntityRegistry {

    public static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, Form1040.MOD_ID);

    public static final RegistryObject<EntityType<TaxCollectorEntity>> TAX_COLLECTOR = register("tax_collector", TaxCollectorEntity::new, EntityClassification.MISC, 0.6f, 1.95f);

    private EntityRegistry() {
    }

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.IFactory<T> factory, EntityClassification classification, float width, float height) {
        return TYPES.register(name, () -> EntityType.Builder.of(factory, classification).sized(width, height).build(name));
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(TAX_COLLECTOR.get(), TaxCollectorEntity.createMobAttributes().build());
    }

}
