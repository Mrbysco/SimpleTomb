package com.lothrazar.simpletomb.helper;

import javax.annotation.Nullable;
import com.lothrazar.library.core.BlockPosDim;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class NBTHelper {

  private static CompoundTag getOrCreateTag(ItemStack stack) {
    return stack.getOrCreateTag();
  }

  private static void setBlockPos(CompoundTag tag, String keyName, BlockPos keyValue) {
    tag.putInt(keyName + "X", keyValue.getX());
    tag.putInt(keyName + "Y", keyValue.getY());
    tag.putInt(keyName + "Z", keyValue.getZ());
  }

  private static BlockPos getBlockPos(@Nullable CompoundTag tag, String keyName) {
    return (tag != null &&
        tag.contains(keyName + "X") &&
        tag.contains(keyName + "Y") &&
        tag.contains(keyName + "Z"))
            ? new BlockPos(
                tag.getInt(keyName + "X"),
                tag.getInt(keyName + "Y"),
                tag.getInt(keyName + "Z"))
            : BlockPos.ZERO;
  }

  public static ItemStack setLocation(ItemStack stack, String keyName, BlockPosDim location) {
    setLocation(getOrCreateTag(stack), keyName, location);
    return stack;
  }

  private static CompoundTag setLocation(CompoundTag tag, String keyName, BlockPosDim location) {
    setBlockPos(tag, keyName, location.toBlockPos());
    tag.putString(keyName + "D", location.getDimension());
    return tag;
  }

  public static BlockPosDim getLocation(ItemStack stack, String keyName) {
    return getLocation(getOrCreateTag(stack), keyName);
  }

  private static BlockPosDim getLocation(@Nullable CompoundTag tag, String keyName) {
    if (tag != null && tag.contains(keyName + "D")) {
      BlockPos pos = getBlockPos(tag, keyName);
      if (!pos.equals(BlockPos.ZERO)) {
        return new BlockPosDim(pos, tag.getString(keyName + "D"));
      }
    }
    return BlockPosDim.ORIGIN;
  }
}
