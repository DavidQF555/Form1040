package io.github.davidqf555.minecraft.f1040.common;

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

    public final ForgeConfigSpec.DoubleValue taxRate, shadyBankerRate, bribeSuccessRate;
    public final ForgeConfigSpec.IntValue indebtedAmt, villageRange, ironGolemCount, taxCollectorMin, taxCollectorMax;
    public final ForgeConfigSpec.BooleanValue roundUp, persistent;
    public final ForgeConfigSpec.LongValue taxPeriod;

    public ServerConfigs(ForgeConfigSpec.Builder builder) {
        builder.push("Server config for Form 1040 mod");
        villageRange = builder.comment("This is the range from a village that players get taxed. Set to -1 for unlimited range. ")
                .defineInRange("range", -1, -1, Integer.MAX_VALUE);
        taxRate = builder.comment("This is the proportion of each item in players' inventories that is taxed. ")
                .defineInRange("rate", 0.1, 0, Double.MAX_VALUE);
        indebtedAmt = builder.comment("This is the number of different taxed items for each player that are required for tax collectors and iron golems to attack them. ")
                .defineInRange("indebted", 5, 0, Integer.MAX_VALUE);
        roundUp = builder.comment("This is whether the number of items taxed is rounded up. ")
                .define("roundUp", true);
        persistent = builder.comment("This is whether debt remains after death. ")
                .define("persistent", true);
        taxPeriod = builder.comment("This is the period in between the time that players are taxed. ")
                .defineInRange("period", 24000, 1, Long.MAX_VALUE);
        ironGolemCount = builder.comment("This is the number of iron golems that spawn every time a indebted player is taxed. ")
                .defineInRange("ironGolem", 2, 0, Integer.MAX_VALUE);
        taxCollectorMin = builder.comment("This is the minimum distance tax collectors and iron golems will spawn from the player. Must be less than or equal to maxDist. ")
                .defineInRange("minDist", 8, 0, Integer.MAX_VALUE);
        taxCollectorMax = builder.comment("This is the maximum distance tax collectors and iron golems will spawn from the player. Must be greater than or equal to minDist. ")
                .defineInRange("minDist", 12, 1, Integer.MAX_VALUE);
        shadyBankerRate = builder.comment("This is the chance that a Shady Banker spawns every tax period for each player. ")
                .defineInRange("shadyBankerRate", 0.2, 0, 1);
        bribeSuccessRate = builder.comment("This is the chance that bribing a Tax Collector is successful. ")
                .defineInRange("bribeSuccessRate", 0.25, 0, 1);
        builder.pop();
    }
}
