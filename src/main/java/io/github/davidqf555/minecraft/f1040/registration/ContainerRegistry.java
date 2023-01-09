package io.github.davidqf555.minecraft.f1040.registration;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.items.OffshoreBankAccountContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ContainerRegistry {

    public static final DeferredRegister<MenuType<?>> TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Form1040.MOD_ID);

    private ContainerRegistry() {
    }

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> register(String name, IContainerFactory<T> factory) {
        return TYPES.register(name, () -> IForgeMenuType.create(factory));
    }

    public static final RegistryObject<MenuType<OffshoreBankAccountContainer>> OFFSHORE_BANK_ACCOUNT = register("offshore_bank_account", OffshoreBankAccountContainer::new);


}
