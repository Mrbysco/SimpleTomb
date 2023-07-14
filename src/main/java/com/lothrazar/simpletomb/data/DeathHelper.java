package com.lothrazar.simpletomb.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.lothrazar.library.core.BlockPosDim;
import net.minecraft.world.entity.player.Player;

public class DeathHelper {

  public static final DeathHelper INSTANCE = new DeathHelper();
  private final Map<UUID, BlockPosDim> lastGraveList = new HashMap<>();

  public BlockPosDim getLastGrave(Player player) {
    return lastGraveList.getOrDefault(player.getGameProfile().getId(), BlockPosDim.ORIGIN);
  }

  public BlockPosDim deleteLastGrave(Player player) {
    return lastGraveList.remove(player.getGameProfile().getId());
  }

  public BlockPosDim putLastGrave(Player player, BlockPosDim loc) {
    return lastGraveList.put(player.getGameProfile().getId(), loc);
  }
}
