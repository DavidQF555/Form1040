package io.github.davidqf555.minecraft.tax.common;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ServerConfigs {

    public static final ServerConfigs INSTANCE;
    public static final ForgeConfigSpec SPEC;

    static {
        Pair<ServerConfigs, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(ServerConfigs::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    public final ForgeConfigSpec.ConfigValue<Double> taxRate, taxCollectorRate;
    public final ForgeConfigSpec.ConfigValue<Integer> punishAmt;
    public final ForgeConfigSpec.ConfigValue<Boolean> roundUp, persistent;
    public final ForgeConfigSpec.ConfigValue<Long> taxPeriod;

    public ServerConfigs(ForgeConfigSpec.Builder builder) {
        builder.push("Server config for Tax mod");
        taxRate = builder.comment("This is the proportion of each item in players' inventories that is taxed. ")
                .defineInRange("rate", 0.1, 0, Double.MAX_VALUE);
        taxCollectorRate = builder.comment("This is the chance that a Tax Collector spawns near players every day. ")
                .defineInRange("spawnRate", 0.5, 0, 1);
        punishAmt = builder.comment("This is the number of different taxed items for each player that are required for Tax Collectors to attack them. ")
                .defineInRange("punish", 5, 0, Integer.MAX_VALUE);
        roundUp = builder.comment("This is whether the number of items taxed is rounded up. ")
                .define("roundUp", true);
        persistent = builder.comment("This is whether debt remains after death. ")
                .define("persistent", true);
        taxPeriod = builder.comment("This is the period in between the time that players are taxed. ")
                .defineInRange("period", 24000, 1, Long.MAX_VALUE);
        builder.pop();
    }
}
