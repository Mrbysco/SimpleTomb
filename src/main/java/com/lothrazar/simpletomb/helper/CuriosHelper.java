package com.lothrazar.simpletomb.helper;

import com.lothrazar.simpletomb.ModTomb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;
import java.util.Set;

public class CuriosHelper {

  public static boolean autoEquip(ItemStack stack, Player player) {
    Set<String> tags = CuriosApi.getItemStackSlots(stack, player.level()).keySet();
    ICuriosItemHandler handler = CuriosApi.getCuriosInventory(player).orElse(null);
    if (handler == null) {
      return false;
    }
    Map<String, ICurioStacksHandler> curios = handler.getCurios();
    for (String tag : tags) {
      ICurioStacksHandler curioStacks = curios.get(tag);
      if (curioStacks != null) {
        IDynamicStackHandler current = curioStacks.getStacks();
        for (int s = 0; s < current.getSlots(); s++) {
          // Do the thing
          stack = current.insertItem(s, stack, false);
	        ModTomb.LOGGER.info("{} result {}", s, stack);
          if (stack.isEmpty()) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
