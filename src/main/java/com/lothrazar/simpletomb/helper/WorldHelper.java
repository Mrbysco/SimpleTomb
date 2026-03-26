package com.lothrazar.simpletomb.helper;

import com.lothrazar.simpletomb.ConfigTomb;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.ArrayList;
import java.util.List;

public class WorldHelper {

  public static float getRandom(RandomSource rand, double min, double max) {
    return (float) (rand.nextDouble() * (max - min) + min);
  }

  public static boolean isValidPlacement(Level level, BlockPos myPos) {
    if (level.isOutsideBuildHeight(myPos)) {
      return false;
    }
    BlockState blockState = level.getBlockState(myPos);
    return blockState.isAir() || blockState.getBlock() == Blocks.WATER;
  }

  public static GlobalPos findGraveSpawn(final Player player, final BlockPos initPos) {
    final int xRange = ConfigTomb.HSEARCHRANGE.get();
    final int yRange = ConfigTomb.VSEARCHRANGE.get();
    final int zRange = ConfigTomb.HSEARCHRANGE.get();
    Level level = player.level();
    if (isValidPlacement(level, initPos)
        && isValidSolid(level, initPos)) {
      return new GlobalPos(level.dimension(), initPos);
    }
    List<BlockPos> positionsWithSolidBelow = new ArrayList<>();
    List<BlockPos> positions = new ArrayList<>();
    for (int x = initPos.getX() - xRange; x < initPos.getX() + xRange; x++) {
      for (int y = initPos.getY() - yRange; y < initPos.getY() + yRange; y++) {
        for (int z = initPos.getZ() - zRange; z < initPos.getZ() + zRange; z++) {
          BlockPos myPos = new BlockPos(x, y, z);
          boolean isValid = isValidPlacement(level, myPos);
          if (!isValid) {
            continue;
          }
          if (isValidSolid(level, myPos)) {
            positionsWithSolidBelow.add(myPos);
          }
          else {
            positions.add(myPos);
          }
        }
      }
    }
    BlockPos found = null;
    if (positionsWithSolidBelow.size() > 0) {
      sortByDistance(initPos, positionsWithSolidBelow);
      found = positionsWithSolidBelow.getFirst();
    }
    else if (positions.size() > 0) {
      sortByDistance(initPos, positions);
      found = positions.getFirst();
    }
    else {
      return null;
    }
    return new GlobalPos(level.dimension(), found);
  }

  private static void sortByDistance(final BlockPos initPos, List<BlockPos> positions) {
    positions.sort((pos0, pos1) -> {
      double dist0 = Math.sqrt(pos0.distSqr(initPos));
      double dist1 = Math.sqrt(pos1.distSqr(initPos));
      return Double.compare(dist0, dist1);
    });
  }

  private static boolean isValidSolid(Level level, BlockPos myPos) {
    return level.getBlockState(myPos.below()).canOcclude();
  }

  public static BlockPos getInitialPos(Level level, BlockPos pos) {
    WorldBorder border = level.getWorldBorder();
    boolean validXZ = border.isWithinBounds(pos);
    boolean validY = !level.isOutsideBuildHeight(pos);
    if (validXZ && validY) {
      return pos;
    }
    else {
      int x = pos.getX();
      int y = pos.getY();
      int z = pos.getZ();
      if (!validXZ) {
        x = Math.min(Math.max(pos.getX(), (int) border.getMinX()), (int) border.getMaxX());
        z = Math.min(Math.max(pos.getZ(), (int) border.getMinZ()), (int) border.getMaxZ());
      }
      if (!validY) {
        if (y < 1) {
          y = 1;
        }
        if (y > level.getMaxY()) {
          y = level.getMaxY() - 1;
        }
      }
      return new BlockPos(x, y, z);
    }
  }

  public static boolean isRuleKeepInventory(Player player) {
    return isRuleKeepInventory(player.level());
  }

  public static boolean isRuleKeepInventory(Level level) {
    return level instanceof ServerLevel serverLevel && serverLevel.getGameRules().get(GameRules.KEEP_INVENTORY);
  }

  public static void removeNoEvent(Level level, BlockPos pos) {
    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
  }

  public static boolean placeGrave(Level level, BlockPos pos, BlockState state) {
    return level.setBlock(pos, state, 2);
  }

  public static boolean isNight(Level level) {
    long dayTime = level.getDayTime() % 24000L;
    return dayTime >= 13000L && dayTime < 23000L;
  }

  public static float[] getHSBtoRGBF(float hue, float saturation, float brightness) {
    int r = 0;
    int g = 0;
    int b = 0;
    if (saturation == 0.0F) {
      r = g = b = (int) (brightness * 255.0F + 0.5F);
    }
    else {
      float h = (hue - (float) Math.floor(hue)) * 6.0F;
      float f = h - (float) Math.floor(h);
      float p = brightness * (1.0F - saturation);
      float q = brightness * (1.0F - saturation * f);
      float t = brightness * (1.0F - saturation * (1.0F - f));
      switch ((int) h) {
        case 0:
          r = (int) (brightness * 255.0F + 0.5F);
          g = (int) (t * 255.0F + 0.5F);
          b = (int) (p * 255.0F + 0.5F);
          break;
        case 1:
          r = (int) (q * 255.0F + 0.5F);
          g = (int) (brightness * 255.0F + 0.5F);
          b = (int) (p * 255.0F + 0.5F);
          break;
        case 2:
          r = (int) (p * 255.0F + 0.5F);
          g = (int) (brightness * 255.0F + 0.5F);
          b = (int) (t * 255.0F + 0.5F);
          break;
        case 3:
          r = (int) (p * 255.0F + 0.5F);
          g = (int) (q * 255.0F + 0.5F);
          b = (int) (brightness * 255.0F + 0.5F);
          break;
        case 4:
          r = (int) (t * 255.0F + 0.5F);
          g = (int) (p * 255.0F + 0.5F);
          b = (int) (brightness * 255.0F + 0.5F);
          break;
        case 5:
          r = (int) (brightness * 255.0F + 0.5F);
          g = (int) (p * 255.0F + 0.5F);
          b = (int) (q * 255.0F + 0.5F);
      }
    }
    return new float[] {
            r / 255.0F,
            g / 255.0F,
            b / 255.0F,
    };
  }

  public static float[] getRGBColor3F(int color) {
    return new float[] {
            ARGB.red(color) / 255.0F,
            ARGB.green(color) / 255.0F,
            ARGB.blue(color) / 255.0F,
    };
  }

  public static Component getDimensionName(ResourceKey<Level> levelResourceKey) {
    Identifier dimLocation = levelResourceKey.identifier();
    return Component.translatableWithFallback(dimLocation.toLanguageKey("dimension"), dimLocation.toString());
  }
}
