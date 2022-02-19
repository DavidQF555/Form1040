package io.github.davidqf555.minecraft.tax.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.davidqf555.minecraft.tax.client.model.MixedVillagerModel;
import io.github.davidqf555.minecraft.tax.common.Tax;
import io.github.davidqf555.minecraft.tax.common.entities.TaxCollectorEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.util.ResourceLocation;

public class TaxCollectorRenderer extends MobRenderer<TaxCollectorEntity, MixedVillagerModel<TaxCollectorEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tax.MOD_ID, "textures/entity/tax_collector.png");

    public TaxCollectorRenderer(EntityRendererManager manager) {
        super(manager, new MixedVillagerModel<>(0), 0.5f);
        this.addLayer(new HeldItemLayer<TaxCollectorEntity, MixedVillagerModel<TaxCollectorEntity>>(this) {
            @Override
            public void render(MatrixStack p_225628_1_, IRenderTypeBuffer p_225628_2_, int p_225628_3_, TaxCollectorEntity p_225628_4_, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
                if (p_225628_4_.isAggressive()) {
                    super.render(p_225628_1_, p_225628_2_, p_225628_3_, p_225628_4_, p_225628_5_, p_225628_6_, p_225628_7_, p_225628_8_, p_225628_9_, p_225628_10_);
                }
            }
        });
        addLayer(new UniformLayerRenderer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(TaxCollectorEntity entity) {
        return TEXTURE;
    }
}
