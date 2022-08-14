package io.github.davidqf555.minecraft.f1040.registration;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

public final class TagRegistry {

    public static final ITag.INamedTag<Item> TAX_EXEMPT = ItemTags.bind(new ResourceLocation(Form1040.MOD_ID, "tax_exempt").toString());

    private TagRegistry() {
    }

}
