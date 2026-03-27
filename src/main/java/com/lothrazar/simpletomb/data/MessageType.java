package com.lothrazar.simpletomb.data;

import com.lothrazar.simpletomb.ModTomb;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

public enum MessageType {

  MESSAGE_FAIL_TO_PLACE_GRAVE("message.fail_to_place_grave"),
  //open grave
  MESSAGE_OPEN_GRAVE_SUCCESS("message.open_grave.success"), MESSAGE_OPEN_GRAVE_NEED_OWNER("message.open_grave.need_owner"),
  //
  MESSAGE_NO_LOOT_FOR_GRAVE("message.no_loot_for_grave"), MESSAGE_NO_PLACE_FOR_GRAVE("message.no_place_for_grave"),
  //rendered as text in world on gravestone
  MESSAGE_DAY("message.day"), MESSAGE_RIP("message.rip"),
  //logs or tooltips
  MESSAGE_NEW_GRAVE("message.new_grave"), MESSAGE_JOURNEYMAP("message.journeymap"), MESSAGE_DISTANCE("message.distance");

  public static final Style MESSAGE_SPECIAL;
  static {
    MESSAGE_SPECIAL = Style.EMPTY.withColor(ChatFormatting.GOLD);
  }
  private final String key;

  MessageType(String key) {
    this.key = key;
  }

  public String getKey() {
    return ModTomb.MODID + "." + this.key;
  }

  public Component getTranslationWithStyle(Style style, Object... params) {
    return Component.translatable(getKey(), params).setStyle(style);
  }

  public String getTranslation(Object... params) {
    return Component.translatable(getKey(), params).getString();
  }

  public void sendSpecialMessage(Player sender, Object... params) {
    sender.sendSystemMessage(this.getTranslationWithStyle(MESSAGE_SPECIAL, params));
  }
}
