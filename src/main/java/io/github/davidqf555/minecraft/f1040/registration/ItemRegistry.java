package io.github.davidqf555.minecraft.f1040.registration;

import com.mojang.datafixers.util.Pair;
import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.items.OffshoreBankAccountItem;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Form1040.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ItemRegistry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Form1040.MOD_ID);
    private static final List<Pair<ResourceKey<CreativeModeTab>, Supplier<? extends Item>>> TABS = new ArrayList<>();
    private static final ResourceKey<CreativeModeTab> TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(Form1040.MOD_ID, "main"));

    public static final RegistryObject<OffshoreBankAccountItem> OFFSHORE_BANK_ACCOUNT = register("offshore_bank_account", getTab(), () -> new OffshoreBankAccountItem(new Item.Properties().stacksTo(1)));

    private ItemRegistry() {
    }

    @SubscribeEvent
    public static void onFMLCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB.location(), CreativeModeTab.builder().icon(Items.PAPER::getDefaultInstance).title(Component.translatable(Util.makeDescriptionId("itemGroup", TAB.location()))).build());
        });
    }

    @SubscribeEvent
    public static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        TABS.stream().filter(pair -> event.getTabKey().equals(pair.getFirst())).map(Pair::getSecond).forEach(event::accept);
    }

    public static ResourceKey<CreativeModeTab> getTab() {
        return TAB;
    }

    private static <T extends Item> RegistryObject<T> register(String name, ResourceKey<CreativeModeTab> tab, Supplier<T> item) {
        RegistryObject<T> out = ITEMS.register(name, item);
        if (tab != null) {
            TABS.add(Pair.of(tab, out));
        }
        return out;
    }

}
