package io.github.davidqf555.minecraft.f1040.common.player;

import io.github.davidqf555.minecraft.f1040.common.ServerConfigs;
import io.github.davidqf555.minecraft.f1040.common.world.data.GovernmentData;
import io.github.davidqf555.minecraft.f1040.registration.TagRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Debt implements INBTSerializable<CompoundTag> {

    public static Capability<Debt> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    private final Map<Item, Integer> debt;

    public Debt() {
        debt = new HashMap<>();
    }

    public static Debt get(Player player) {
        return player.getCapability(CAPABILITY).orElseGet(Debt::new);
    }

    public static boolean isIndebted(Player player) {
        return get(player).isIndebted();
    }

    public static boolean add(Player player, double rate) {
        Map<Item, Integer> unique = new HashMap<>();
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && !stack.is(TagRegistry.TAX_EXEMPT)) {
                Item item = stack.getItem();
                unique.put(item, unique.getOrDefault(item, 0) + stack.getCount());
            }
        }
        if (!unique.isEmpty()) {
            if (ServerConfigs.INSTANCE.roundUp.get()) {
                Item rand = List.copyOf(unique.keySet()).get(player.getRandom().nextInt(unique.size()));
                get(player).addDebt(rand, Mth.ceil(unique.get(rand) * rate));
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
                Item rand = List.copyOf(tax.keySet()).get(player.getRandom().nextInt(tax.size()));
                get(player).addDebt(rand, tax.get(rand));
                return true;
            }
        }
        return false;
    }

    public static void pay(Player player, int id) {
        Debt debt = get(player);
        for (Item item : debt.getAllDebt()) {
            ContainerHelper.clearOrCountMatchingItems(player.getInventory(), stack -> stack.getItem().equals(item), debt.getDebt(item), false);
        }
        Map<Item, Integer> items = new HashMap<>();
        debt.getAllDebt().forEach(item -> items.put(item, debt.getDebt(item)));
        GovernmentData.add((ServerLevel) player.level, id, items);
        debt.clear();
        GovernmentRelations relations = GovernmentRelations.get(player);
        relations.setTaxFactor(relations.getTaxFactor() * ServerConfigs.INSTANCE.taxDecreaseRate.get());
    }

    public static boolean canPay(Player player) {
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

    public boolean canPay(Player player, Item item) {
        return ContainerHelper.clearOrCountMatchingItems(player.getInventory(), stack -> stack.getItem().equals(item), 0, true) >= getDebt(item);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        getAllDebt().forEach(item -> nbt.putInt(ForgeRegistries.ITEMS.getKey(item).toString(), getDebt(item)));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for (String key : nbt.getAllKeys()) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key));
            if (item != null) {
                addDebt(item, nbt.getInt(key));
            }
        }
    }

}
