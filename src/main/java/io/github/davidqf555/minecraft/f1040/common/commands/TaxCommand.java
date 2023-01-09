package io.github.davidqf555.minecraft.f1040.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.ServerConfigs;
import io.github.davidqf555.minecraft.f1040.common.player.Debt;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;

public final class TaxCommand {

    private static final String ADD = "commands." + Form1040.MOD_ID + ".add";
    private static final String CLEAR = "commands." + Form1040.MOD_ID + ".clear";

    private TaxCommand() {
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
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

    private static int add(CommandSource source, Collection<ServerPlayerEntity> targets) {
        return add(source, targets, 1);
    }

    private static int add(CommandSource source, Collection<ServerPlayerEntity> targets, int amount) {
        int success = 0;
        for (ServerPlayerEntity player : targets) {
            boolean added = false;
            for (int i = 0; i < amount; i++) {
                if (Debt.add(player, ServerConfigs.INSTANCE.taxRate.get()) && !added) {
                    success++;
                    added = true;
                }
            }
        }
        source.sendSuccess(new TranslationTextComponent(ADD, success), true);
        return success;
    }

    private static int clear(CommandSource source, Collection<ServerPlayerEntity> targets) {
        for (ServerPlayerEntity player : targets) {
            Debt.get(player).clear();
        }
        int size = targets.size();
        source.sendSuccess(new TranslationTextComponent(CLEAR, size), true);
        return size;
    }
}
