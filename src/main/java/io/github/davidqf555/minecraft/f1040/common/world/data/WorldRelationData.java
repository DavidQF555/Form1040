package io.github.davidqf555.minecraft.f1040.common.world.data;

import io.github.davidqf555.minecraft.f1040.common.Form1040;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldRelationData extends WorldSavedData {

    private static final ITextComponent[] NAMES = new ITextComponent[]{
            new TranslationTextComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name1"))),
            new TranslationTextComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name2"))),
            new TranslationTextComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name3"))),
            new TranslationTextComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name4"))),
            new TranslationTextComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name5"))),
            new TranslationTextComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name6"))),
            new TranslationTextComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name7"))),
            new TranslationTextComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name8"))),
            new TranslationTextComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name9"))),
            new TranslationTextComponent(Util.makeDescriptionId("entity", new ResourceLocation(Form1040.MOD_ID, "gov.name10"))),
    };
    public static final int MAX = NAMES.length;
    private static final String NAME = Form1040.MOD_ID + "_Relations";
    private final RelationData[] data = new RelationData[MAX];

    public WorldRelationData() {
        super(NAME);
        for (int i = 0; i < data.length; i++) {
            data[i] = new RelationData();
        }
    }

    public static ITextComponent getName(int id) {
        return NAMES[id];
    }

    private static WorldRelationData getOrCreate(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(WorldRelationData::new, NAME);
    }

    private static RelationData getRelationData(MinecraftServer server, int entity) {
        return getOrCreate(server).data[entity];
    }

    public static double getRelativeShare(MinecraftServer server, int entity, UUID player) {
        return getRelationData(server, entity).getRelativeShare(player);
    }

    public static void multiplyShare(MinecraftServer server, int entity, UUID player, double factor) {
        getRelationData(server, entity).multiplyShare(player, factor);
    }

    @Override
    public void load(CompoundNBT tag) {
        ListNBT data = new ListNBT();
        Arrays.stream(this.data).map(RelationData::serializeNBT).forEach(data::add);
        tag.put("Data", data);
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        if (tag.contains("Data", Constants.NBT.TAG_LIST)) {
            ListNBT data = tag.getList("Data", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < Math.min(data.size(), this.data.length); i++) {
                this.data[i].deserializeNBT(data.getCompound(i));
            }
        }
        return tag;
    }

    private static class RelationData implements INBTSerializable<CompoundNBT> {

        private final Map<UUID, Double> relation = new HashMap<>();

        private double getRelativeShare(UUID player) {
            double sum = relation.keySet().stream().mapToDouble(this::getAbsoluteShare).sum();
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
        public CompoundNBT serializeNBT() {
            CompoundNBT tag = new CompoundNBT();
            relation.forEach((id, value) -> tag.putDouble(id.toString(), value));
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            for (String id : nbt.getAllKeys()) {
                if (nbt.contains(id, Constants.NBT.TAG_DOUBLE)) {
                    relation.put(UUID.fromString(id), nbt.getDouble(id));
                }
            }
        }

    }

}
