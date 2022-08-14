package io.github.davidqf555.minecraft.f1040.client.render;

import io.github.davidqf555.minecraft.f1040.client.model.MixedVillagerModel;
import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.entities.ShadyBankerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ShadyBankerRenderer extends MobRenderer<ShadyBankerEntity, VillagerModel<ShadyBankerEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Form1040.MOD_ID, "textures/entity/extended_villager.png");

    public ShadyBankerRenderer(EntityRendererProvider.Context manager) {
        super(manager, new VillagerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(MixedVillagerModel.EXTENDED_VILLAGER)), 0.5f);
        addLayer(new OverlayLayerRenderer<>(this, new ResourceLocation(Form1040.MOD_ID, "textures/entity/suit.png")));
        addLayer(new OverlayLayerRenderer<>(this, new ResourceLocation(Form1040.MOD_ID, "textures/entity/sunglasses.png")));
    }

    @Override
    public ResourceLocation getTextureLocation(ShadyBankerEntity entity) {
        return TEXTURE;
    }

}
