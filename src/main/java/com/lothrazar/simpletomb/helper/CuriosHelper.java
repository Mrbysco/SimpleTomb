package com.lothrazar.simpletomb.helper;

import com.lothrazar.simpletomb.TombComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;

public class CuriosHelper {

  public static void tagEquippedCurios(Player player) {
    ICuriosItemHandler handler = CuriosApi.getCuriosInventory(player).orElse(null);
    if (handler == null) {
      return;
    }
    for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
      IDynamicStackHandler stacks = entry.getValue().getStacks();
      for (int i = 0; i < stacks.getSlots(); i++) {
        ItemStack stack = stacks.getStackInSlot(i);
        if (!stack.isEmpty()) {
          stack.set(TombComponents.CURIO_SLOT.get(),
              new TombComponents.CurioSlot(entry.getKey(), i));
        }
      }
    }
  }

  public static boolean restoreToSlot(Player player, String slotType, int slotIndex, ItemStack stack) {
    ICuriosItemHandler handler = CuriosApi.getCuriosInventory(player).orElse(null);
    if (handler == null) {
      return false;
    }
    ICurioStacksHandler slotHandler = handler.getCurios().get(slotType);
    if (slotHandler == null) {
      return false;
    }
    IDynamicStackHandler stacks = slotHandler.getStacks();
    if (slotIndex >= stacks.getSlots()) {
      return false;
    }
    if (!stacks.getStackInSlot(slotIndex).isEmpty()) {
      return false;
    }
    stacks.setStackInSlot(slotIndex, stack);
    return true;
  }
}
