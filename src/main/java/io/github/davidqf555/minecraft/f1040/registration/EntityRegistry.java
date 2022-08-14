package io.github.davidqf555.minecraft.f1040.registration;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.entities.ShadyBankerEntity;
import io.github.davidqf555.minecraft.f1040.common.entities.TaxCollectorEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Form1040.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class EntityRegistry {

    public static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Form1040.MOD_ID);

    public static final RegistryObject<EntityType<TaxCollectorEntity>> TAX_COLLECTOR = register("tax_collector", TaxCollectorEntity::new, MobCategory.MISC, 0.6f, 1.95f);
    public static final RegistryObject<EntityType<ShadyBankerEntity>> SHADY_BANKER = register("shady_banker", ShadyBankerEntity::new, MobCategory.MISC, 0.6f, 1.95f);

    private EntityRegistry() {
    }

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.EntityFactory<T> factory, MobCategory classification, float width, float height) {
        return TYPES.register(name, () -> EntityType.Builder.of(factory, classification).sized(width, height).build(name));
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(TAX_COLLECTOR.get(), TaxCollectorEntity.createMobAttributes().build());
        event.put(SHADY_BANKER.get(), Villager.createAttributes().build());
    }

}
