package com.lothrazar.simpletomb.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathHelper {
  public static final GlobalPos ORIGIN = GlobalPos.of(ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("simpletomb", "origin")), BlockPos.ZERO);
  public static final DeathHelper INSTANCE = new DeathHelper();
  private final Map<UUID, GlobalPos> lastGraveList = new HashMap<>();

  public GlobalPos getLastGrave(Player player) {
    return lastGraveList.getOrDefault(player.getGameProfile().getId(), ORIGIN);
  }

  public GlobalPos deleteLastGrave(Player player) {
    return lastGraveList.remove(player.getGameProfile().getId());
  }

  public GlobalPos putLastGrave(Player player, GlobalPos loc) {
    return lastGraveList.put(player.getGameProfile().getId(), loc);
  }
}
