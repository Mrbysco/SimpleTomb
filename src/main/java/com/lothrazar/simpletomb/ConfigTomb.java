package com.lothrazar.simpletomb;

import com.lothrazar.simpletomb.data.PartEnum;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class ConfigTomb {

  public static final ModConfigSpec CONFIG;
  public static BooleanValue TOMBENABLED;
  public static BooleanValue KEYGIVEN;
  public static BooleanValue KEYNAMED;
  public static IntValue TOMBEXTRAITEMS;
  public static IntValue TPSURVIVAL;
  public static BooleanValue TPCREATIVE;
  public static BooleanValue TOMBLOG;
  public static BooleanValue TOMBCHAT;
  public static IntValue VSEARCHRANGE;
  public static IntValue HSEARCHRANGE;
  public static BooleanValue KEYOPENONUSE;
  public static ModConfigSpec.EnumValue<PartEnum> KEEPPARTS;
  static final String WALL = "####################################################################################";
  static {
    final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    BUILDER.comment(WALL, "Simple Tomb config", WALL).push(ModTomb.MODID);
    BUILDER.comment(WALL).push("tomb");
    //
    TOMBENABLED = BUILDER.comment("\r\nWhether to handle player death at all (false will disable almost the entire mod)")
        .define("enabled", true);
    TOMBEXTRAITEMS = BUILDER.comment("\r\nThe radius in which extra bonus items should be hunted for and collected when a grave is spawned; set to zero (0) to disable")
        .defineInRange("extra_items", 2, 0, 16);
    TOMBLOG = BUILDER.comment("\r\nIf true, write to the game log (server log) every time a tomb is placed")
        .define("log", true);
    TOMBCHAT = BUILDER.comment("\r\nIf true, send a player chat message every time a tomb is placed")
        .define("chat", true);
    VSEARCHRANGE = BUILDER.comment("\r\nWhen searching for a grave location, this is the maximum height to check")
        .defineInRange("search_height", 16, 2, 128);
    HSEARCHRANGE = BUILDER.comment("\r\nWhen searching for a grave location, this is the maximum range to check")
        .defineInRange("search_range", 8, 2, 128);
    KEEPPARTS = BUILDER.comment("\r\nKeep parts of the inventory")
        .defineEnum("keep_parts", PartEnum.NONE);
    BUILDER.pop();
    BUILDER.comment(WALL).push("key");
    KEYGIVEN = BUILDER.comment("\r\nWhether to give a Grave Key item to the player on death.  Tomb can be opened without they key, but the key will help the player locate the grave")
        .define("given", true);
    KEYNAMED = BUILDER.comment("\r\nIf a key is being dropped, will the player's display name be added to the tomb key item name")
        .define("named", true);
    KEYOPENONUSE = BUILDER.comment("\r\nTrue means the key will open the grave on use, even if the player is not standing on top")
        .define("openOnUse", true);
    BUILDER.pop();
    //done    
    BUILDER.comment(WALL).push("teleport");
    TPSURVIVAL = BUILDER.comment("\r\nWhen survival player is within this (straight line calculated) distance from the tomb, they can teleport to the tomb.  "
        + "Set as zero (0) to disable survival TP feature.  "
        + " Set as negative one (-1) to allow survival teleportation always and ignore the distance (within dimension) ")
        .defineInRange("survival", 16, -1, 128);
    TPCREATIVE = BUILDER.comment("\r\nIf creative mode players can teleport to the tomb with the key, ignoring distance")
        .define("creative", true);
    BUILDER.pop();
    //
    BUILDER.pop();
    CONFIG = BUILDER.build();
  }
}
