package com.lothrazar.simpletomb.block;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class TombRenderState extends BlockEntityRenderState {

  public String ownerName = "";
  public long deathDate;
  public int timer;
  public ModelTomb graveModel = ModelTomb.GRAVE_SIMPLE;
  public Direction facing = Direction.NORTH;
  public boolean hasOwner;
  public boolean isNight;
}
