package io.github.davidqf555.minecraft.f1040.registration;

import com.mojang.datafixers.util.Pair;
import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.items.OffshoreBankAccountItem;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Form1040.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ItemRegistry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Form1040.MOD_ID);
    private static final List<Pair<Supplier<CreativeModeTab>, Supplier<? extends Item>>> TABS = new ArrayList<>();
    private static CreativeModeTab tab = null;

    public static final RegistryObject<OffshoreBankAccountItem> OFFSHORE_BANK_ACCOUNT = register("offshore_bank_account", ItemRegistry::getTab, () -> new OffshoreBankAccountItem(new Item.Properties().stacksTo(1)));

    private ItemRegistry() {
    }

    @SubscribeEvent
    public static void onRegisterCreativeModeTab(CreativeModeTabEvent.Register event) {
        tab = event.registerCreativeModeTab(new ResourceLocation(Form1040.MOD_ID, "main"), builder -> builder.icon(Items.PAPER::getDefaultInstance).title(Component.translatable(Util.makeDescriptionId("itemGroup", new ResourceLocation(Form1040.MOD_ID, "main")))));
    }

    @SubscribeEvent
    public static void onBuildCreativeModeTabContents(CreativeModeTabEvent.BuildContents event) {
        TABS.stream().filter(pair -> event.getTab().equals(pair.getFirst().get())).map(Pair::getSecond).forEach(event::accept);
    }

    public static CreativeModeTab getTab() {
        return tab;
    }

    private static <T extends Item> RegistryObject<T> register(String name, Supplier<CreativeModeTab> tab, Supplier<T> item) {
        RegistryObject<T> out = ITEMS.register(name, item);
        if (tab != null) {
            TABS.add(Pair.of(tab, out));
        }
        return out;
    }

}
