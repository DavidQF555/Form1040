package io.github.davidqf555.minecraft.tax.common;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;

public final class TaxCommand {

    private static final String ADD = "commands." + Tax.MOD_ID + ".add";
    private static final String CLEAR = "commands." + Tax.MOD_ID + ".clear";

    private TaxCommand() {
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("tax")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.literal("add")
                                .executes(context -> add(context.getSource(), EntityArgument.getPlayers(context, "targets")))
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(context -> add(context.getSource(), EntityArgument.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "amount")))
                                )
                        )
                        .then(Commands.literal("clear")
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
                if (Debt.add(player) && !added) {
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
