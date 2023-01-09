package io.github.davidqf555.minecraft.f1040.common.world.data;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import io.github.davidqf555.minecraft.f1040.common.ServerConfigs;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class GovernmentData extends SavedData {

    private static final Component[] NAMES = new Component[]{
            new TranslatableComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name1"))),
            new TranslatableComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name2"))),
            new TranslatableComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name3"))),
            new TranslatableComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name4"))),
            new TranslatableComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name5"))),
            new TranslatableComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name6"))),
            new TranslatableComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name7"))),
            new TranslatableComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name8"))),
            new TranslatableComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name9"))),
            new TranslatableComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name10"))),
    };
    public static final int MAX = NAMES.length;
    private static final String NAME = Form1040.MOD_ID + "_Government";
    private final RelationData[] relations = new RelationData[MAX];
    private final GovernmentInventory[] inventories = new GovernmentInventory[MAX];

    public GovernmentData() {
        for (int i = 0; i < MAX; i++) {
            relations[i] = new RelationData();
            inventories[i] = new GovernmentInventory();
        }
    }

    public GovernmentData(CompoundTag tag) {
        this();
        if (tag.contains("Relations", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Relations", Tag.TAG_COMPOUND);
            for (int i = 0; i < Math.min(list.size(), MAX); i++) {
                relations[i].deserializeNBT(list.getCompound(i));
            }
        }
        if (tag.contains("Inventories", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Inventories", Tag.TAG_COMPOUND);
            for (int i = 0; i < Math.min(list.size(), MAX); i++) {
                inventories[i].deserializeNBT(list.getCompound(i));
            }
        }
    }

    public static Component getName(int id) {
        return NAMES[id];
    }

    public static double getRelativeShare(MinecraftServer server, int id, UUID player) {
        return getOrCreate(server).relations[id].getRelativeShare(player);
    }

    public static boolean isCorrupted(ServerLevel world, int id, UUID player) {
        return getRelativeShare(world.getServer(), id, player) >= ServerConfigs.INSTANCE.corruptionThreshold.get();
    }

    public static void multiplyShare(MinecraftServer server, int id, UUID player, double factor) {
        getOrCreate(server).relations[id].multiplyShare(player, factor);
    }

    public static void add(ServerLevel world, int id, Map<Item, Integer> items) {
        GovernmentInventory inventory = getOrCreate(world).get(id);
        items.forEach(inventory::add);
    }

    public static List<ItemStack> removeRandom(ServerLevel world, int id, Random random, double factor) {
        return getOrCreate(world).get(id).removeRandom(random, factor);
    }

    public static GovernmentData getOrCreate(ServerLevel world) {
        return getOrCreate(world.getServer());
    }

    public static GovernmentData getOrCreate(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(GovernmentData::new, GovernmentData::new, NAME);
    }

    public GovernmentInventory get(int id) {
        return inventories[id];
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag relations = new ListTag();
        for (RelationData relation : this.relations) {
            relations.add(relation.serializeNBT());
        }
        tag.put("Relations", relations);
        ListTag inventories = new ListTag();
        for (GovernmentInventory inventory : this.inventories) {
            inventories.add(inventory.serializeNBT());
        }
        tag.put("Inventories", inventories);
        return tag;
    }

    private static class GovernmentInventory implements INBTSerializable<CompoundTag> {

        private final Map<Item, Integer> items = new HashMap<>();

        private NonNullList<ItemStack> removeRandom(Random random, double factor) {
            NonNullList<ItemStack> out = NonNullList.create();
            for (Item key : items.keySet()) {
                int count = items.get(key);
                int amt = Mth.floor(count * factor * random.nextDouble());
                items.put(key, count - amt);
                ItemStack stack = key.getDefaultInstance();
                stack.setCount(amt);
                out.add(stack);
            }
            return out;
        }

        private void add(Item item, int count) {
            items.put(item, items.getOrDefault(item, 0) + count);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            items.forEach((item, count) -> tag.putInt(item.getRegistryName().toString(), count));
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            for (String key : nbt.getAllKeys()) {
                if (nbt.contains(key, Tag.TAG_INT)) {
                    int count = nbt.getInt(key);
                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key));
                    add(item, count);
                }
            }
        }

    }

    private static class RelationData implements INBTSerializable<CompoundTag> {

        private final Map<UUID, Double> relation = new HashMap<>();

        private double getRelativeShare(UUID player) {
            double sum = relation.keySet().stream().mapToDouble(this::getAbsoluteShare).sum() + 2;
            if (sum <= 0) {
                return 0;
            }
            return getAbsoluteShare(player) / sum;
        }

        private double getAbsoluteShare(UUID player) {
            return relation.computeIfAbsent(player, id -> 1.0);
        }

        private void multiplyShare(UUID player, double factor) {
            if (relation.containsKey(player)) {
                relation.put(player, relation.get(player) * factor);
            } else {
                relation.put(player, factor);
            }
            for (UUID id : relation.keySet()) {
                if (!id.equals(player)) {
                    relation.put(id, getAbsoluteShare(id) / factor);
                }
            }
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            relation.forEach((id, value) -> tag.putDouble(id.toString(), value));
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            for (String id : nbt.getAllKeys()) {
                if (nbt.contains(id, Tag.TAG_DOUBLE)) {
                    relation.put(UUID.fromString(id), nbt.getDouble(id));
                }
            }
        }

    }

}
