package io.github.davidqf555.minecraft.f1040.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.davidqf555.minecraft.f1040.common.items.OffshoreBankAccountContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class OffshoreBankAccountScreen extends AbstractContainerScreen<OffshoreBankAccountContainer> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/hopper.png");

    public OffshoreBankAccountScreen(OffshoreBankAccountContainer container, Inventory player, Component name) {
        super(container, player, name);
        imageHeight = 133;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    public void render(PoseStack matrix, int mouseX, int mouseY, float partial) {
        renderBackground(matrix);
        super.render(matrix, mouseX, mouseY, partial);
        renderTooltip(matrix, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack matrix, float partial, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        blit(matrix, x, y, 0, 0, imageWidth, imageHeight);
    }

}
