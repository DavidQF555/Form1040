package io.github.davidqf555.minecraft.tax.common;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod("tax")
public class Tax {

    public static final String MOD_ID = "tax";

    public Tax() {
        MinecraftForge.EVENT_BUS.register(this);
    }
}
