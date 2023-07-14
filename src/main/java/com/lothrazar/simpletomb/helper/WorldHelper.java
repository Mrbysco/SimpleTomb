package com.lothrazar.simpletomb.helper;

import java.util.ArrayList;
import java.util.List;
import com.lothrazar.library.core.BlockPosDim;
import com.lothrazar.library.util.BlockPosUtil;
import com.lothrazar.simpletomb.ConfigTomb;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;

public class WorldHelper {

  public static float getRandom(RandomSource rand, double min, double max) {
    return (float) (rand.nextDouble() * (max - min) + min);
  }

  public static boolean isValidPlacement(Level level, BlockPos myPos) {
    //0 is the bottom bedrock level
    //so if we place there, players cant place a block under it to stand safely
    if (level.isOutsideBuildHeight(myPos)) {
      // blockstate doesnt matter, out of world
      return false;
    }
    //    FluidState fluidHere = world.getFluidState(myPos);
    //only air or water. not any fluid state, and not any waterlogged block
    BlockState blockState = level.getBlockState(myPos);
    return blockState.isAir() || blockState.getBlock() == Blocks.WATER; // && fluidHere.getFluid().isIn(FluidTags.WATER));
  }

  public static BlockPosDim findGraveSpawn(final Player player, final BlockPos initPos) {
    final int xRange = ConfigTomb.HSEARCHRANGE.get();
    final int yRange = ConfigTomb.VSEARCHRANGE.get();
    final int zRange = ConfigTomb.HSEARCHRANGE.get();
    Level level = player.level();
    //   
    //shortcut: if the death position is valid AND solid base. JUST DO THAT dont even search
    if (isValidPlacement(level, initPos)
        && isValidSolid(level, initPos)) {
      //      ModTomb.LOGGER.info(" initPos is enough =  " + initPos);
      return new BlockPosDim(initPos, level);
    }
    //
    //    ModTomb.LOGGER.info(isValidINIT + "find initPos=  " + initPos);
    List<BlockPos> positionsWithSolidBelow = new ArrayList<>();
    List<BlockPos> positions = new ArrayList<>();
    for (int x = initPos.getX() - xRange; x < initPos.getX() + xRange; x++) {
      for (int y = initPos.getY() - yRange; y < initPos.getY() + yRange; y++) {
        for (int z = initPos.getZ() - zRange; z < initPos.getZ() + zRange; z++) {
          BlockPos myPos = new BlockPos(x, y, z);
          //
          boolean isValid = isValidPlacement(level, myPos);
          //          ModTomb.LOGGER.info("isvalid  initPos=  " + isValid);
          if (!isValid) {
            continue;
          }
          //where do we put this
          if (isValidSolid(level, myPos)) {
            //this is better
            positionsWithSolidBelow.add(myPos);
          }
          else {
            positions.add(myPos);
          }
        }
      }
    }
    //first, if we have a 'solid pase' pos, use that
    BlockPos found = null;
    if (positionsWithSolidBelow.size() > 0) {
      //use this one 
      BlockPosUtil.sortByDistance(initPos, positionsWithSolidBelow);
      found = positionsWithSolidBelow.get(0);
    }
    else if (positions.size() > 0) {
      //i guess it has to float in the air
      BlockPosUtil.sortByDistance(initPos, positions);
      found = positions.get(0);
    }
    else {
      return null;
    }
    return new BlockPosDim(found, level);
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
        if (y > level.getMaxBuildHeight()) {
          y = level.getMaxBuildHeight() - 1;
        }
      }
      return new BlockPos(x, y, z);
    }
  }

  public static boolean isRuleKeepInventory(Player player) {
    return isRuleKeepInventory(player.level());
  }

  public static boolean isRuleKeepInventory(Level level) {
    return level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
  }

  public static void removeNoEvent(Level level, BlockPos pos) {
    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
  }

  public static boolean placeGrave(Level level, BlockPos pos, BlockState state) {
    return level.setBlock(pos, state, 2);
  }
}
