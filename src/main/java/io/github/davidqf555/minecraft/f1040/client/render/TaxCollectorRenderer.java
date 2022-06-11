package io.github.davidqf555.minecraft.f1040.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.davidqf555.minecraft.f1040.client.model.MixedVillagerModel;
import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.entities.TaxCollectorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class TaxCollectorRenderer extends MobRenderer<TaxCollectorEntity, MixedVillagerModel<TaxCollectorEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Form1040.MOD_ID, "textures/entity/tax_collector.png");

    public TaxCollectorRenderer(EntityRendererProvider.Context context) {
        super(context, new MixedVillagerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(MixedVillagerModel.LOCATION)), 0.5f);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()) {
            @Override
            public void render(PoseStack p_116352_, MultiBufferSource p_116353_, int p_116354_, TaxCollectorEntity p_116355_, float p_116356_, float p_116357_, float p_116358_, float p_116359_, float p_116360_, float p_116361_) {
                if (p_116355_.isAggressive()) {
                    super.render(p_116352_, p_116353_, p_116354_, p_116355_, p_116356_, p_116357_, p_116358_, p_116359_, p_116360_, p_116361_);
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
