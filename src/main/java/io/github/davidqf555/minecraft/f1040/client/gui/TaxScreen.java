package io.github.davidqf555.minecraft.f1040.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.packets.PayTaxesPacket;
import io.github.davidqf555.minecraft.f1040.common.packets.StopPayingPacket;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class TaxScreen extends Screen {

    private static final Component TITLE = MutableComponent.create(new TranslatableContents("gui." + Form1040.MOD_ID + ".tax_screen")), PAY = MutableComponent.create(new TranslatableContents("gui." + Form1040.MOD_ID + ".tax_screen.pay"));
    private static final ResourceLocation TEXTURE = new ResourceLocation(Form1040.MOD_ID, "textures/gui/tax_screen.png");
    private static final int TEXTURE_WIDTH = 176, TEXTURE_HEIGHT = 184, X_SIZE = 176, Y_SIZE = 166, SLOT_WIDTH = 18, SLOT_HEIGHT = 18;
    private final Slot[] items;
    private final boolean canPay;
    private final UUID collector;
    private int x, y;

    public TaxScreen(NonNullList<ItemStack> items, boolean canPay, UUID collector) {
        super(TITLE);
        this.items = items.stream().map(item -> new Slot(item, 0, 0, SLOT_WIDTH, SLOT_HEIGHT)).toArray(Slot[]::new);
        this.canPay = canPay;
        this.collector = collector;
    }

    @Override
    protected void init() {
        x = (width - X_SIZE) / 2;
        y = (height - Y_SIZE) / 2;
        int rows = Mth.ceil(items.length / 9.0);
        int startY = y + (Y_SIZE - rows * SLOT_HEIGHT) / 2;
        for (int i = 0; i < items.length; i++) {
            Slot slot = items[i];
            int row = i / 9;
            int col = i % 9;
            slot.setY(startY + row * SLOT_HEIGHT);
            int rowWidth = Math.min(9, items.length - row * 9) * SLOT_WIDTH;
            slot.setX(x + (X_SIZE - rowWidth) / 2 + col * SLOT_WIDTH);
            addRenderableWidget(slot);
        }
        addRenderableWidget(new PayButton(x + (X_SIZE - 40) / 2, y + Y_SIZE - 36, 40, 18));
    }

    @Override
    public void render(PoseStack matrix, int mouseX, int mouseY, float partial) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(matrix, x, y, 0, 0, X_SIZE, Y_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        font.draw(matrix, title, x + (X_SIZE - font.width(title)) / 2f, y + 6, 0xFF404040);
        super.render(matrix, mouseX, mouseY, partial);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int p_97765_, int p_97766_, int p_97767_) {
        InputConstants.Key mouseKey = InputConstants.getKey(p_97765_, p_97766_);
        if (super.keyPressed(p_97765_, p_97766_, p_97767_)) {
            return true;
        } else if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        Form1040.CHANNEL.sendToServer(new StopPayingPacket(collector));
    }

    private class Slot extends AbstractWidget {

        private final ItemStack item;

        public Slot(ItemStack item, int x, int y, int width, int height) {
            super(x, y, width, height, item.getDisplayName());
            this.item = item;
        }

        @Override
        public void renderButton(PoseStack matrix, int mouseX, int mouseY, float partial) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            blit(matrix, x, y, 120, 166, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            ItemRenderer renderer = minecraft.getItemRenderer();
            int itemX = x + (width - 16) / 2;
            int itemY = y + (height - 16) / 2;
            renderer.renderAndDecorateItem(item, itemX, itemY);
            renderer.renderGuiItemDecorations(font, item, itemX, itemY, item.getCount() + "");
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }

    }

    private class PayButton extends AbstractButton {

        public PayButton(int x, int y, int width, int height) {
            super(x, y, width, height, PAY);
        }

        @Override
        public void renderButton(PoseStack matrix, int mouseX, int mouseY, float partial) {
            int xStart;
            int color = 0xFFFFFFFF;
            if (!isHoveredOrFocused()) {
                xStart = 0;
            } else if (canPay) {
                xStart = 40;
            } else {
                xStart = 80;
                color = 0xFFFF0000;
            }
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            blit(matrix, x, y, xStart, 166, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            drawCenteredString(matrix, font, getMessage(), x + width / 2, y + (height - font.lineHeight) / 2, color);
        }

        @Override
        public void onPress() {
            if (canPay) {
                Form1040.CHANNEL.sendToServer(new PayTaxesPacket(collector));
                onClose();
            }
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }

    }

}
