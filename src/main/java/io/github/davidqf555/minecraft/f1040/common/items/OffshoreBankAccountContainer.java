package io.github.davidqf555.minecraft.f1040.common.items;

import io.github.davidqf555.minecraft.f1040.registration.ContainerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;

public class OffshoreBankAccountContainer extends Container {

    private final IInventory inventory;

    public OffshoreBankAccountContainer(@Nullable ContainerType<?> type, int id, PlayerInventory player, IInventory inventory) {
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

    public OffshoreBankAccountContainer(int id, PlayerInventory player, IInventory inventory) {
        this(ContainerRegistry.OFFSHORE_BANK_ACCOUNT.get(), id, player, inventory);
    }

    public OffshoreBankAccountContainer(int id, PlayerInventory player) {
        this(id, player, new Inventory(5));
    }

    public OffshoreBankAccountContainer(int id, PlayerInventory player, PacketBuffer data) {
        this(id, player);
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
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
    public void removed(PlayerEntity player) {
        super.removed(player);
        inventory.stopOpen(player);
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return inventory.stillValid(player);
    }

}
