package com.lothrazar.simpletomb.data;

import com.lothrazar.simpletomb.ModTomb;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
      ListTag glist = data.getList(ModTomb.MODID, CompoundTag.TAG_COMPOUND);
      for (int i = 0; i < glist.size(); i++) {
        this.playerGraves.add(glist.getCompound(i));
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
    return NbtUtils.readBlockPos(grave, "pos").get();
  }

  public static ResourceKey<Level> getDim(CompoundTag grave) {
    ResourceLocation dim = ResourceLocation.parse(grave.getString("dim"));
    return ResourceKey.create(Registries.DIMENSION, dim);
  }

  public static List<ItemStack> getDrops(CompoundTag grave, HolderLookup.Provider provider) {
    ListTag drops = grave.getList("drops", 10);
    List<ItemStack> done = new ArrayList<ItemStack>();
    for (int i = 0; i < drops.size(); i++) {
      done.add(ItemStack.parseOptional(provider, drops.getCompound(i)));
    }
    return done;
  }

  public void deleteAll() {
    this.playerGraves = new ArrayList<>();
  }

  /**
   * because mojang is pretty stupid and made this client only so copy paste
   */
  public String getCoordinatesAsString(BlockPos pos) {
    return "" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
  }

  public String toDisplayString(int i, HolderLookup.Provider provider) {
    CompoundTag gd = getGrave(i);
    return String.format("[%d] (%s) (%s) {%d}", i, getDim(gd), getCoordinatesAsString(getPos(gd)), getDrops(gd, provider).size());
  }
}
