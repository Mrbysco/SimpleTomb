package com.lothrazar.simpletomb.helper;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosSlotTypes;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;
import java.util.Set;

public class CuriosHelper {

  public static boolean autoEquip(ItemStack stack, Player player) {
    Set<String> tags = CuriosSlotTypes.getItemSlotTypes(stack, player.level().isClientSide()).keySet();
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
          stack = current.insertItem(s, stack, false);
          if (stack.isEmpty()) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
