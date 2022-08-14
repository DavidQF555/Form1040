package io.github.davidqf555.minecraft.f1040.registration;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.items.OffshoreBankAccountContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ContainerRegistry {

    public static final DeferredRegister<ContainerType<?>> TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, Form1040.MOD_ID);

    private ContainerRegistry() {
    }

    private static <T extends Container> RegistryObject<ContainerType<T>> register(String name, IContainerFactory<T> factory) {
        return TYPES.register(name, () -> IForgeContainerType.create(factory));
    }    public static final RegistryObject<ContainerType<OffshoreBankAccountContainer>> OFFSHORE_BANK_ACCOUNT = register("offshore_bank_account", OffshoreBankAccountContainer::new);




}
