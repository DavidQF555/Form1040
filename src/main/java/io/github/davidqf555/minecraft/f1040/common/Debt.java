package io.github.davidqf555.minecraft.f1040.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.*;

public class Debt implements INBTSerializable<CompoundNBT> {

    private final Map<Item, Integer> debt;

    public Debt() {
        debt = new HashMap<>();
    }

    public static Debt get(PlayerEntity player) {
        return player.getCapability(Provider.capability).orElseGet(Debt::new);
    }

    public static boolean isIndebted(PlayerEntity player) {
        return get(player).isIndebted();
    }

    public static boolean add(PlayerEntity player) {
        Map<Item, Integer> unique = new HashMap<>();
        for (ItemStack stack : player.inventory.items) {
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                unique.put(item, unique.getOrDefault(item, 0) + stack.getCount());
            }
        }
        if (!unique.isEmpty()) {
            double rate = ServerConfigs.INSTANCE.taxRate.get();
            if (ServerConfigs.INSTANCE.roundUp.get()) {
                Item rand = new ArrayList<>(unique.keySet()).get(player.getRandom().nextInt(unique.size()));
                get(player).addDebt(rand, MathHelper.ceil(unique.get(rand) * rate));
                return true;
            }
            Map<Item, Integer> tax = new HashMap<>();
            for (Map.Entry<Item, Integer> entry : unique.entrySet()) {
                int total = (int) (entry.getValue() * rate);
                if (total >= 1) {
                    tax.put(entry.getKey(), total);
                }
            }
            if (!tax.isEmpty()) {
                Item rand = new ArrayList<>(tax.keySet()).get(player.getRandom().nextInt(tax.size()));
                get(player).addDebt(rand, tax.get(rand));
                return true;
            }
        }
        return false;
    }

    public static void pay(PlayerEntity player) {
        Debt debt = get(player);
        for (Item item : debt.getAllDebt()) {
            ItemStackHelper.clearOrCountMatchingItems(player.inventory, stack -> stack.getItem().equals(item), debt.getDebt(item), false);
        }
        debt.clear();
    }

    public static boolean canPay(PlayerEntity player) {
        Debt debt = get(player);
        for (Item item : debt.getAllDebt()) {
            if (!debt.canPay(player, item)) {
                return false;
            }
        }
        return true;
    }

    public boolean isIndebted() {
        return getAllDebt().size() >= ServerConfigs.INSTANCE.indebtedAmt.get();
    }

    public void addDebt(Item item, int amt) {
        debt.put(item, debt.getOrDefault(item, 0) + amt);
    }

    public int getDebt(Item item) {
        return debt.getOrDefault(item, 0);
    }

    public Set<Item> getAllDebt() {
        return debt.keySet();
    }

    public void clear() {
        debt.clear();
    }

    public boolean canPay(PlayerEntity player, Item item) {
        return ItemStackHelper.clearOrCountMatchingItems(player.inventory, stack -> stack.getItem().equals(item), 0, true) >= getDebt(item);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        getAllDebt().forEach(item -> nbt.putInt(item.getRegistryName().toString(), getDebt(item)));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        for (String key : nbt.getAllKeys()) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key));
            if (item != null) {
                addDebt(item, nbt.getInt(key));
            }
        }
    }

    public static class Provider implements ICapabilitySerializable<INBT> {

        @CapabilityInject(Debt.class)
        public static Capability<Debt> capability = null;
        private final LazyOptional<Debt> instance = LazyOptional.of(() -> Objects.requireNonNull(capability.getDefaultInstance()));

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
            return cap == capability ? instance.cast() : LazyOptional.empty();
        }

        @Override
        public INBT serializeNBT() {
            return capability.getStorage().writeNBT(capability, instance.orElseThrow(NullPointerException::new), null);
        }

        @Override
        public void deserializeNBT(INBT nbt) {
            capability.getStorage().readNBT(capability, instance.orElseThrow(NullPointerException::new), null, nbt);
        }
    }

    public static class Storage implements Capability.IStorage<Debt> {

        @Override
        public INBT writeNBT(Capability<Debt> capability, Debt instance, Direction side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<Debt> capability, Debt instance, Direction side, INBT nbt) {
            instance.deserializeNBT((CompoundNBT) nbt);
        }
    }
}
