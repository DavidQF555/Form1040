package io.github.davidqf555.minecraft.f1040.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.ServerConfigs;
import io.github.davidqf555.minecraft.f1040.common.player.Debt;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public final class TaxCommand {

    private static final String ADD = "commands." + Form1040.MOD_ID + ".add";
    private static final String CLEAR = "commands." + Form1040.MOD_ID + ".clear";

    private TaxCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tax")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("add")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> add(context.getSource(), EntityArgument.getPlayers(context, "targets")))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> add(context.getSource(), EntityArgument.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "amount")))
                                )
                        )
                )
                .then(Commands.literal("clear")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> clear(context.getSource(), EntityArgument.getPlayers(context, "targets")))
                        )
                )
        );
    }

    private static int add(CommandSourceStack source, Collection<ServerPlayer> targets) {
        return add(source, targets, 1);
    }

    private static int add(CommandSourceStack source, Collection<ServerPlayer> targets, int amount) {
        int success = 0;
        for (ServerPlayer player : targets) {
            boolean added = false;
            for (int i = 0; i < amount; i++) {
                if (Debt.add(player, ServerConfigs.INSTANCE.taxRate.get()) && !added) {
                    success++;
                    added = true;
                }
            }
        }
        source.sendSuccess(new TranslatableComponent(ADD, success), true);
        return success;
    }

    private static int clear(CommandSourceStack source, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            Debt.get(player).clear();
        }
        int size = targets.size();
        source.sendSuccess(new TranslatableComponent(CLEAR, size), true);
        return size;
    }
}
