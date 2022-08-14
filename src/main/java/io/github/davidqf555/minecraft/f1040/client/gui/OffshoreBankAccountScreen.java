package io.github.davidqf555.minecraft.f1040.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.davidqf555.minecraft.f1040.common.items.OffshoreBankAccountContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class OffshoreBankAccountScreen extends ContainerScreen<OffshoreBankAccountContainer> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/hopper.png");

    public OffshoreBankAccountScreen(OffshoreBankAccountContainer container, PlayerInventory player, ITextComponent name) {
        super(container, player, name);
        imageHeight = 133;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partial) {
        renderBackground(matrix);
        super.render(matrix, mouseX, mouseY, partial);
        renderTooltip(matrix, mouseX, mouseY);
    }

    @Override
    protected void renderBg(MatrixStack matrix, float partial, int mouseX, int mouseY) {
        minecraft.getTextureManager().bind(TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        blit(matrix, x, y, 0, 0, imageWidth, imageHeight);
    }

}
