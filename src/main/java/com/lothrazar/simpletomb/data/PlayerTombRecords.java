package com.lothrazar.simpletomb.data;

import com.lothrazar.simpletomb.ModTomb;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerTombRecords {

  UUID playerId;
  public List<CompoundTag> playerGraves = new ArrayList<>();

  public PlayerTombRecords(UUID id, CompoundTag first) {
    playerId = id;
    playerGraves.add(first);
  }

  public PlayerTombRecords() {}

  public CompoundTag getGrave(int index) {
    if (index >= playerGraves.size()) {
      return null;
    }
    return playerGraves.get(index);
  }

  public void read(CompoundTag data, UUID playerId) {
    this.playerId = playerId;
    if (data.contains(ModTomb.MODID)) {
      ListTag glist = data.getListOrEmpty(ModTomb.MODID);
      for (int i = 0; i < glist.size(); i++) {
        this.playerGraves.add(glist.getCompoundOrEmpty(i));
      }
    }
  }

  public CompoundTag write() {
    CompoundTag data = new CompoundTag();
    ListTag glist = new ListTag();
    glist.addAll(playerGraves);
    data.put(ModTomb.MODID, glist);
    return data;
  }

  public static BlockPos getPos(CompoundTag grave) {
    return BlockPos.CODEC.parse(NbtOps.INSTANCE, grave.get("pos")).result().orElse(BlockPos.ZERO);
  }

  public static ResourceKey<Level> getDim(CompoundTag grave) {
    Identifier dim = Identifier.parse(grave.getStringOr("dimension", "minecraft:overworld"));
    return ResourceKey.create(Registries.DIMENSION, dim);
  }

  public static List<ItemStack> getDrops(CompoundTag grave, HolderLookup.Provider provider) {
    ListTag drops = grave.getListOrEmpty("drops");
    List<ItemStack> done = new ArrayList<ItemStack>();
    for (int i = 0; i < drops.size(); i++) {
      done.add(ItemStack.CODEC.parse(
          provider.createSerializationContext(NbtOps.INSTANCE),
          drops.getCompoundOrEmpty(i)
      ).result().orElse(ItemStack.EMPTY));
    }
    return done;
  }

  public void deleteAll() {
    this.playerGraves = new ArrayList<>();
  }

  public String toDisplayString(int i, HolderLookup.Provider provider) {
    CompoundTag gd = getGrave(i);
    return String.format("[%d] (%s) (%s) {%d}", i, getDim(gd),getPos(gd).toShortString(), getDrops(gd, provider).size());
  }
}
