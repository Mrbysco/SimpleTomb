package com.lothrazar.simpletomb.helper;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

public class EntityHelper {

  public static final String NBT_PLAYER_PERSISTED = "PlayerPersisted";

  public static boolean autoEquip(ItemStack stack, Player player) {
    if (stack.isEmpty()) {
      return false;
    }
    Identifier registryName = BuiltInRegistries.ITEM.getKey(stack.getItem());
    if (registryName == null) {
      return false;
    }
    if (EnchantmentHelper.getTagEnchantmentLevel(player.level().holderOrThrow(Enchantments.BINDING_CURSE), stack) > 0) {
      return false;
    }
    if (stack.getMaxStackSize() == 1) {
      if (ModList.get().isLoaded("curios")) {
        if (CuriosHelper.autoEquip(stack, player)) {
          return true;
        }
      }
      if (player.getOffhandItem().isEmpty()) {
        if (stack.is(Items.SHIELD)) {
          player.setItemSlot(EquipmentSlot.OFFHAND, stack.copy());
          return true;
        }
      }
      EquipmentSlot slot = stack.getEquipmentSlot();
      boolean isElytra = false;
      if (slot == null) {
        if (!stack.is(Items.ELYTRA)) {
          return false;
        }
        slot = EquipmentSlot.CHEST;
        isElytra = true;
      }
      else if (slot == EquipmentSlot.CHEST) {
        isElytra = stack.is(Items.ELYTRA);
      }
      int armorSlotIndex = 36 + slot.getIndex();
      ItemStack stackInSlot = player.getInventory().getItem(armorSlotIndex);
      if (stackInSlot.isEmpty()) {
        player.getInventory().setItem(armorSlotIndex, stack.copy());
        return true;
      }
      if (slot != EquipmentSlot.CHEST) {
        return false;
      }
      if (isElytra) {
        player.getInventory().add(stackInSlot.copy());
        player.getInventory().setItem(armorSlotIndex, stack.copy());
        return true;
      }
    }
    return false;
  }

  public static boolean isValidPlayer(@Nullable Entity entity) {
    return entity instanceof Player && !(entity instanceof FakePlayer);
  }

  public static boolean isValidPlayerMP(@Nullable Entity entity) {
    return isValidPlayer(entity) && !entity.level().isClientSide();
  }

  public static CompoundTag getPersistentTag(Player player) {
    CompoundTag persistentData = player.getPersistentData();
    CompoundTag persistentTag;
    if (persistentData.contains(NBT_PLAYER_PERSISTED)) {
      persistentTag = persistentData.getCompoundOrEmpty(NBT_PLAYER_PERSISTED);
      return persistentTag;
    }
    else {
      persistentTag = new CompoundTag();
      persistentData.put(NBT_PLAYER_PERSISTED, persistentTag);
      return persistentTag;
    }
  }
}
