package io.github.davidqf555.minecraft.tax.client.render;

import io.github.davidqf555.minecraft.tax.common.Tax;
import io.github.davidqf555.minecraft.tax.common.entities.TaxCollectorEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.VillagerModel;
import net.minecraft.util.ResourceLocation;

public class TaxCollectorRenderer extends MobRenderer<TaxCollectorEntity, VillagerModel<TaxCollectorEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tax.MOD_ID, "textures/entity/tax_collector.png");

    public TaxCollectorRenderer(EntityRendererManager manager) {
        super(manager, new VillagerModel<>(0), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(TaxCollectorEntity entity) {
        return TEXTURE;
    }
}
