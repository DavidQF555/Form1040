package io.github.davidqf555.minecraft.f1040.common.player;

import io.github.davidqf555.minecraft.f1040.common.ServerConfigs;
import io.github.davidqf555.minecraft.f1040.common.world.data.GovernmentData;
import io.github.davidqf555.minecraft.f1040.registration.TagRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class Debt implements INBTSerializable<CompoundNBT> {

    @CapabilityInject(Debt.class)
    public static Capability<Debt> capability = null;
    private final Map<Item, Integer> debt;

    public Debt() {
        debt = new HashMap<>();
    }

    public static Debt get(PlayerEntity player) {
        return player.getCapability(capability).orElseGet(Debt::new);
    }

    public static boolean isIndebted(PlayerEntity player) {
        return get(player).isIndebted();
    }

    public static boolean add(PlayerEntity player, double rate) {
        Map<Item, Integer> items = new HashMap<>();
        for (ItemStack stack : player.inventory.items) {
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                items.put(item, items.getOrDefault(item, 0) + stack.getCount());
            }
        }
        Map<Item, Integer> tax = new HashMap<>();
        items.forEach((item, count) -> {
            int amt = getTaxedAmount(item, count, rate);
            if (amt > 0) {
                tax.put(item, amt);
            }
        });
        if (tax.isEmpty()) {
            return false;
        }
        List<Item> keys = new ArrayList<>(tax.keySet());
        Item item = keys.get(player.getRandom().nextInt(keys.size()));
        get(player).addDebt(item, tax.get(item));
        return true;
    }

    public static void pay(PlayerEntity player, int id) {
        Debt debt = get(player);
        for (Item item : debt.getAllDebt()) {
            ItemStackHelper.clearOrCountMatchingItems(player.inventory, stack -> stack.getItem().equals(item), debt.getDebt(item), false);
        }
        Map<Item, Integer> items = new HashMap<>();
        debt.getAllDebt().forEach(item -> items.put(item, debt.getDebt(item)));
        GovernmentData.add((ServerWorld) player.level, id, items);
        debt.clear();
        GovernmentRelations relations = GovernmentRelations.get(player);
        relations.setTaxFactor(relations.getTaxFactor() * ServerConfigs.INSTANCE.taxDecreaseRate.get());
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

    public static int getTaxedAmount(Item item, int count, double rate) {
        if (ServerConfigs.INSTANCE.invertExempt.get() ^ TagRegistry.TAX_EXEMPT.contains(item)) {
            return 0;
        }
        if (ServerConfigs.INSTANCE.roundUp.get()) {
            return MathHelper.ceil(count * rate);
        }
        return MathHelper.floor(count * rate);
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

}
