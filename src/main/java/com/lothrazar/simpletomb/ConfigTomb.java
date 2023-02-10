package com.lothrazar.simpletomb;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.lothrazar.PartEnum;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.nio.file.Path;

@EventBusSubscriber(modid = ModTomb.MODID)
public class ConfigTomb {

  private static final ForgeConfigSpec.Builder CFG = new ForgeConfigSpec.Builder();
  private static ForgeConfigSpec COMMON_CONFIG;
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
  public static EnumValue<PartEnum> KEEPPARTS;
  static final String WALL = "####################################################################################";

  public static void setup(Path path) {
    final CommentedFileConfig configData = CommentedFileConfig.builder(path)
        .sync()
        .autosave()
        .writingMode(WritingMode.REPLACE)
        .build();
    configData.load();
    COMMON_CONFIG.setConfig(configData);
  }

  static {
    initConfig();
  }

  private static void initConfig() {
    CFG.comment(WALL, "Simple Tomb config", WALL).push(ModTomb.MODID);
    CFG.comment(WALL).push("tomb");
    //
    TOMBENABLED = CFG.comment("\r\nWhether to handle player death at all (false will disable almost the entire mod)")
        .define("enabled", true);
    TOMBEXTRAITEMS = CFG.comment("\r\nThe radius in which extra bonus items should be hunted for and collected when a grave is spawned; set to zero (0) to disable")
        .defineInRange("extra_items", 2, 0, 16);
    TOMBLOG = CFG.comment("\r\nIf true, write to the game log (server log) every time a tomb is placed")
        .define("log", true);
    TOMBCHAT = CFG.comment("\r\nIf true, send a player chat message every time a tomb is placed")
        .define("chat", true);
    VSEARCHRANGE = CFG.comment("\r\nWhen searching for a grave location, this is the maximum height to check")
        .defineInRange("search_height", 16, 2, 128);
    HSEARCHRANGE = CFG.comment("\r\nWhen searching for a grave location, this is the maximum range to check")
        .defineInRange("search_range", 8, 2, 128);
    KEEPPARTS = CFG.comment("\r\nKeep parts of the inventory")
        .defineEnum("keep_parts", PartEnum.NONE);
    CFG.pop();
    CFG.comment(WALL).push("key");
    KEYGIVEN = CFG.comment("\r\nWhether to give a Grave Key item to the player on death.  Tomb can be opened without they key, but the key will help the player locate the grave")
        .define("given", true);
    KEYNAMED = CFG.comment("\r\nIf a key is being dropped, will the player's display name be added to the tomb key item name")
        .define("named", true);
    KEYOPENONUSE = CFG.comment("\r\nTrue means the key will open the grave on use, even if the player is not standing on top")
        .define("openOnUse", true);
    CFG.pop();
    //done    
    CFG.comment(WALL).push("teleport");
    TPSURVIVAL = CFG.comment("\r\nWhen survival player is within this (straight line calculated) distance from the tomb, they can teleport to the tomb.  "
        + "Set as zero (0) to disable survival TP feature.  "
        + " Set as negative one (-1) to allow survival teleportation always and ignore the distance (within dimension) ")
        .defineInRange("survival", 16, -1, 128);
    TPCREATIVE = CFG.comment("\r\nIf creative mode players can teleport to the tomb with the key, ignoring distance")
        .define("creative", true);
    CFG.pop();
    //
    CFG.pop();
    COMMON_CONFIG = CFG.build();
  }
}
