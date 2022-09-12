package io.github.davidqf555.minecraft.f1040.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class OverlayLayerRenderer<T extends Mob, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private final ResourceLocation texture;

    public OverlayLayerRenderer(RenderLayerParent<T, M> renderer, ResourceLocation texture) {
        super(renderer);
        this.texture = texture;
    }

    @Override
    public void render(PoseStack p_225628_1_, MultiBufferSource p_225628_2_, int p_225628_3_, T p_225628_4_, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
        if (!p_225628_4_.isInvisible()) {
            renderColoredCutoutModel(getParentModel(), getTextureLocation(p_225628_4_), p_225628_1_, p_225628_2_, p_225628_3_, p_225628_4_, 1, 1, 1);
        }
    }

    @Override
    protected ResourceLocation getTextureLocation(T entity) {
        return texture;
    }
}
