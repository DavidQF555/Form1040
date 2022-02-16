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

    public final ForgeConfigSpec.ConfigValue<Double> taxRate;

    public ServerConfigs(ForgeConfigSpec.Builder builder) {
        builder.push("Server config for Tax mod");
        taxRate = builder.comment("This is the proportion of each item in players' inventories that is taxed. ")
                .defineInRange("rate", 0.1, 0, Double.MAX_VALUE);
        builder.pop();
    }
}
