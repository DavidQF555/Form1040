package io.github.davidqf555.minecraft.tax.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.davidqf555.minecraft.tax.common.Tax;
import io.github.davidqf555.minecraft.tax.common.packets.PayTaxesPacket;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TaxScreen extends Screen {

    private static final ITextComponent TITLE = new TranslationTextComponent("gui." + Tax.MOD_ID + ".tax_screen"), PAY = new TranslationTextComponent("gui." + Tax.MOD_ID + ".tax_screen.pay");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Tax.MOD_ID, "textures/gui/tax_screen.png");
    private static final int TEXTURE_WIDTH = 176, TEXTURE_HEIGHT = 184, X_SIZE = 176, Y_SIZE = 166, SLOT_WIDTH = 18, SLOT_HEIGHT = 18;
    private final Slot[] items;
    private final boolean canPay;
    private int x, y;

    public TaxScreen(NonNullList<ItemStack> items, boolean canPay) {
        super(TITLE);
        this.items = items.stream().map(item -> new Slot(item, 0, 0, SLOT_WIDTH, SLOT_HEIGHT)).toArray(Slot[]::new);
        this.canPay = canPay;
    }

    @Override
    protected void init() {
        x = (width - X_SIZE) / 2;
        y = (height - Y_SIZE) / 2;
        int rows = MathHelper.ceil(items.length / 9.0);
        int startY = y + (Y_SIZE - rows * SLOT_HEIGHT) / 2;
        for (int i = 0; i < items.length; i++) {
            Slot slot = items[i];
            int row = i / 9;
            int col = i % 9;
            slot.y = startY + row * SLOT_HEIGHT;
            int rowWidth = Math.min(9, items.length - row * 9) * SLOT_WIDTH;
            slot.x = x + (X_SIZE - rowWidth) / 2 + col * SLOT_WIDTH;
            addButton(slot);
        }
        addButton(new PayButton(x + (X_SIZE - 40) / 2, y + Y_SIZE - 36, 40, 18));
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partial) {
        minecraft.getTextureManager().bind(TEXTURE);
        blit(matrix, x, y, 0, 0, X_SIZE, Y_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        font.draw(matrix, title, x + (X_SIZE - font.width(title)) / 2f, y + 6, 0xFF404040);
        super.render(matrix, mouseX, mouseY, partial);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
        InputMappings.Input mouseKey = InputMappings.getKey(p_231046_1_, p_231046_2_);
        if (super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_)) {
            return true;
        } else if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            onClose();
            return true;
        }
        return false;
    }

    private class Slot extends Widget {

        private final ItemStack item;

        public Slot(ItemStack item, int x, int y, int width, int height) {
            super(x, y, width, height, item.getDisplayName());
            this.item = item;
        }

        @Override
        public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float partial) {
            minecraft.getTextureManager().bind(TEXTURE);
            blit(matrix, x, y, 120, 166, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            ItemRenderer renderer = minecraft.getItemRenderer();
            int itemX = x + (width - 16) / 2;
            int itemY = y + (height - 16) / 2;
            renderer.renderAndDecorateItem(minecraft.player, item, itemX, itemY);
            renderer.renderGuiItemDecorations(font, item, itemX, itemY, item.getCount() + "");
        }

    }

    private class PayButton extends Button {

        public PayButton(int x, int y, int width, int height) {
            super(x, y, width, height, PAY, button -> {
                if (TaxScreen.this.canPay) {
                    Tax.CHANNEL.sendToServer(new PayTaxesPacket());
                    TaxScreen.this.onClose();
                }
            });
        }

        @Override
        public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float partial) {
            int xStart;
            int color = 0xFFFFFFFF;
            if (!isHovered()) {
                xStart = 0;
            } else if (canPay) {
                xStart = 40;
            } else {
                xStart = 80;
                color = 0xFFFF0000;
            }
            minecraft.getTextureManager().bind(TEXTURE);
            blit(matrix, x, y, xStart, 166, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            drawCenteredString(matrix, font, getMessage(), x + width / 2, y + (height - font.lineHeight) / 2, color);
        }
    }
}
