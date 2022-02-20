package io.github.davidqf555.minecraft.f1040.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.davidqf555.minecraft.f1040.client.model.MixedVillagerModel;
import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.entities.TaxCollectorEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class UniformLayerRenderer extends RenderLayer<TaxCollectorEntity, MixedVillagerModel<TaxCollectorEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Form1040.MOD_ID, "textures/entity/uniform.png");

    public UniformLayerRenderer(RenderLayerParent<TaxCollectorEntity, MixedVillagerModel<TaxCollectorEntity>> p_117346_) {
        super(p_117346_);
    }

    @Override
    public void render(PoseStack p_117646_, MultiBufferSource p_117647_, int p_117648_, TaxCollectorEntity p_117649_, float p_117650_, float p_117651_, float p_117652_, float p_117653_, float p_117654_, float p_117655_) {
        if (!p_117649_.isInvisible()) {
            renderColoredCutoutModel(getParentModel(), getTextureLocation(p_117649_), p_117646_, p_117647_, p_117648_, p_117649_, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    protected ResourceLocation getTextureLocation(TaxCollectorEntity p_229139_1_) {
        return TEXTURE;
    }
}
