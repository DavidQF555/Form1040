package io.github.davidqf555.minecraft.f1040.common.items;

import io.github.davidqf555.minecraft.f1040.registration.ContainerRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class OffshoreBankAccountContainer extends AbstractContainerMenu {

    private final Container inventory;

    public OffshoreBankAccountContainer(@Nullable MenuType<?> type, int id, Inventory player, Container inventory) {
        super(type, id);
        this.inventory = inventory;
        checkContainerSize(inventory, 5);
        inventory.startOpen(player.player);
        for (int x = 0; x < 5; x++) {
            addSlot(new Slot(inventory, x, 44 + x * 18, 20));
        }
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; ++x) {
                addSlot(new Slot(player, x + y * 9 + 9, 8 + x * 18, y * 18 + 51));
            }
        }
        for (int x = 0; x < 9; x++) {
            addSlot(new Slot(player, x, 8 + x * 18, 109));
        }
    }

    public OffshoreBankAccountContainer(int id, Inventory player, Container inventory) {
        this(ContainerRegistry.OFFSHORE_BANK_ACCOUNT.get(), id, player, inventory);
    }

    public OffshoreBankAccountContainer(int id, Inventory player) {
        this(id, player, new SimpleContainer(5));
    }

    public OffshoreBankAccountContainer(int id, Inventory player, FriendlyByteBuf data) {
        this(id, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack copy = slot.getItem().copy();
            if (index < inventory.getContainerSize()) {
                if (!moveItemStackTo(copy, inventory.getContainerSize(), slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(copy, 0, inventory.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }
            if (copy.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return stack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        inventory.stopOpen(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory.stillValid(player);
    }

}
