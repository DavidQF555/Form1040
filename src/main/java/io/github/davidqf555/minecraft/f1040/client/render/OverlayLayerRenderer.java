package io.github.davidqf555.minecraft.f1040.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;

public class OverlayLayerRenderer<T extends MobEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {

    private final ResourceLocation texture;

    public OverlayLayerRenderer(IEntityRenderer<T, M> renderer, ResourceLocation texture) {
        super(renderer);
        this.texture = texture;
    }

    @Override
    public void render(MatrixStack p_225628_1_, IRenderTypeBuffer p_225628_2_, int p_225628_3_, T p_225628_4_, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
        if (!p_225628_4_.isInvisible()) {
            renderColoredCutoutModel(getParentModel(), getTextureLocation(p_225628_4_), p_225628_1_, p_225628_2_, p_225628_3_, p_225628_4_, 1, 1, 1);
        }
    }

    @Override
    protected ResourceLocation getTextureLocation(T entity) {
        return texture;
    }
}
