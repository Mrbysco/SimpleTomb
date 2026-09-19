package com.lothrazar.simpletomb.helper;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CuriosHelper {

  public static void tagEquippedCurios(Player player) {
//    ICuriosItemHandler handler = CuriosApi.getCuriosInventory(player).orElse(null);
//    if (handler == null) {
//      return;
//    }
//    for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
//      IDynamicStackHandler stacks = entry.getValue().getStacks();
//      for (int i = 0; i < stacks.getSlots(); i++) {
//        ItemStack stack = stacks.getStackInSlot(i);
//        if (!stack.isEmpty()) {
//          stack.set(TombComponents.CURIO_SLOT.get(),
//              new TombComponents.CurioSlot(entry.getKey(), i));
//        }
//      }
//    }
  }

  public static boolean restoreToSlot(Player player, String slotType, int slotIndex, ItemStack stack) {
//    ICuriosItemHandler handler = CuriosApi.getCuriosInventory(player).orElse(null);
//    if (handler == null) {
//      return false;
//    }
//    ICurioStacksHandler slotHandler = handler.getCurios().get(slotType);
//    if (slotHandler == null) {
//      return false;
//    }
//    IDynamicStackHandler stacks = slotHandler.getStacks();
//    if (slotIndex >= stacks.getSlots()) {
//      return false;
//    }
//    if (!stacks.getStackInSlot(slotIndex).isEmpty()) {
//      return false;
//    }
//    stacks.setStackInSlot(slotIndex, stack);
//    return true;
    return false;
  }
}
