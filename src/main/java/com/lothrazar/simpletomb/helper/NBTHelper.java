package com.lothrazar.simpletomb.helper;

import com.lothrazar.simpletomb.TombComponents;
import com.lothrazar.simpletomb.data.DeathHelper;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;

public class NBTHelper {

  public static ItemStack setLocation(ItemStack stack, GlobalPos location) {
    stack.set(TombComponents.TOMB_POS, location);
    return stack;
  }

  public static GlobalPos getLocation(ItemStack stack) {
    if (stack.has(TombComponents.TOMB_POS)) {
      return stack.get(TombComponents.TOMB_POS);
    }
    return DeathHelper.ORIGIN;
  }
}
