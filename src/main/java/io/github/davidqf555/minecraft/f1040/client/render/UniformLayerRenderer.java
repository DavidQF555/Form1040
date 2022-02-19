package io.github.davidqf555.minecraft.f1040.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.davidqf555.minecraft.f1040.client.model.MixedVillagerModel;
import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.entities.TaxCollectorEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

public class UniformLayerRenderer extends LayerRenderer<TaxCollectorEntity, MixedVillagerModel<TaxCollectorEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Form1040.MOD_ID, "textures/entity/uniform.png");

    public UniformLayerRenderer(IEntityRenderer<TaxCollectorEntity, MixedVillagerModel<TaxCollectorEntity>> p_i50926_1_) {
        super(p_i50926_1_);
    }

    @Override
    public void render(MatrixStack p_225628_1_, IRenderTypeBuffer p_225628_2_, int p_225628_3_, TaxCollectorEntity p_225628_4_, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
        renderColoredCutoutModel(getParentModel(), getTextureLocation(p_225628_4_), p_225628_1_, p_225628_2_, p_225628_3_, p_225628_4_, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected ResourceLocation getTextureLocation(TaxCollectorEntity p_229139_1_) {
        return TEXTURE;
    }
}
