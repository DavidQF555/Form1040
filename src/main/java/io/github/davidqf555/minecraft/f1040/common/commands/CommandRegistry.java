package io.github.davidqf555.minecraft.f1040.common.commands;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Form1040.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommandRegistry {

    private CommandRegistry() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        TaxCommand.register(event.getDispatcher());
    }

}
