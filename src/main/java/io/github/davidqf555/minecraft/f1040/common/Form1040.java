package io.github.davidqf555.minecraft.f1040.common;

import io.github.davidqf555.minecraft.f1040.registration.ContainerRegistry;
import io.github.davidqf555.minecraft.f1040.registration.EntityRegistry;
import io.github.davidqf555.minecraft.f1040.registration.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod("f1040")
public class Form1040 {

    public static final String MOD_ID = "f1040";
    public static final CreativeModeTab GROUP = new CreativeModeTab(MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return Items.PAPER.getDefaultInstance();
        }
    };
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, MOD_ID),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public Form1040() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPEC);
        addRegistries(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void addRegistries(IEventBus bus) {
        EntityRegistry.TYPES.register(bus);
        ItemRegistry.ITEMS.register(bus);
        ContainerRegistry.TYPES.register(bus);
    }

}
