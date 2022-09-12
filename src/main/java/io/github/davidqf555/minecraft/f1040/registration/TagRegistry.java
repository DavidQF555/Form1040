package io.github.davidqf555.minecraft.f1040.registration;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class TagRegistry {

    public static final TagKey<Item> TAX_EXEMPT = ItemTags.create(new ResourceLocation(Form1040.MOD_ID, "tax_exempt"));
    public static final TagKey<Item> BRIBE = ItemTags.create(new ResourceLocation(Form1040.MOD_ID, "bribe"));

    private TagRegistry() {
    }

}
