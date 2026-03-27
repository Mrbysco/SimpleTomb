package com.lothrazar.simpletomb.block;

import com.lothrazar.simpletomb.data.MessageType;
import com.lothrazar.simpletomb.helper.WorldHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RenderTomb implements BlockEntityRenderer<BlockEntityTomb, TombRenderState> {

  private final Font font;

  public RenderTomb(BlockEntityRendererProvider.Context cx) {
    this.font = cx.font();
  }

  private static final String TIME_FORMAT = "HH:mm:ss";
  private static final String DATE_FORMAT = "yyyy/MM/dd";

  @Override
  public TombRenderState createRenderState() {
    return new TombRenderState();
  }

  @Override
  public void extractRenderState(BlockEntityTomb te, TombRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
    BlockEntityRenderer.super.extractRenderState(te, state, partialTick, cameraPos, crumblingOverlay);
    state.hasOwner = te.hasOwner();
    state.ownerName = te.getOwnerName();
    state.deathDate = te.getOwnerDeathTime();
    state.timer = te.timer;
    if (te.getLevel() != null) {
      BlockState knownState = te.getLevel().getBlockState(te.getBlockPos());
      if (knownState.getBlock() instanceof BlockTomb grave) {
        state.facing = knownState.getValue(BlockTomb.FACING);
        state.graveModel = grave.getGraveType();
      }
      state.isNight = WorldHelper.isNight(te.getLevel());
    }
  }

  @Override
  public void submit(TombRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
    if (!state.hasOwner) {
      return;
    }
    Direction facing = state.facing;
    ModelTomb graveModel = state.graveModel;
    int light = 0xf000f0;
    int rotationIndex;
    float modX = 0.5F, modY, modZ = 0.5F;
    float value;
    switch (graveModel) {
      case GRAVE_CROSS:
        value = 0.25f;
        modY = 0.06375f;
      break;
      case GRAVE_NORMAL:
        value = 0.12625f;
        modY = 0.5f;
      break;
      case GRAVE_SIMPLE:
      default:
        value = 0.18875f;
        modY = 0.4f;
      break;
    }
    boolean isCross = graveModel == ModelTomb.GRAVE_CROSS;
    switch (facing) {
      case SOUTH:
        rotationIndex = 0;
        if (isCross) {
          modZ = 1f - value;
        }
        else {
          modZ = value;
        }
      break;
      case WEST:
        rotationIndex = -1;
        if (isCross) {
          modX = value;
        }
        else {
          modX = 1f - value;
        }
      break;
      case EAST:
        rotationIndex = 1;
        if (isCross) {
          modX = 1f - value;
        }
        else {
          modX = value;
        }
      break;
      case NORTH:
      default:
        rotationIndex = 2;
        if (isCross) {
          modZ = value;
        }
        else {
          modZ = 1f - value;
        }
    }
    poseStack.pushPose();
    poseStack.translate(modX, modY, modZ);
    poseStack.mulPose(Axis.XP.rotationDegrees(180f));
    if (isCross) {
      switch (facing) {
        case SOUTH:
          poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
        break;
        case WEST:
          poseStack.mulPose(Axis.ZP.rotationDegrees(90f));
        break;
        case EAST:
          poseStack.mulPose(Axis.ZP.rotationDegrees(-90f));
        break;
        case NORTH:
        default:
          poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        break;
      }
    }
    poseStack.mulPose(Axis.YP.rotationDegrees(-90f * rotationIndex));
    MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
    Font fontRender = this.font;
    int textColor = 0xFFFFFFFF;
    showString(ChatFormatting.BOLD + MessageType.MESSAGE_RIP.getTranslation(), poseStack, bufferSource, fontRender, 0,
        textColor, 0.007f, light);
    showString(ChatFormatting.BOLD + state.ownerName, poseStack, bufferSource, fontRender, 11, textColor, 0.005f, light);
    float scaleForDate = 0.004f;
    long days = state.timer / 24000;
    String dateString = MessageType.MESSAGE_DAY.getTranslation(days);
    showString(ChatFormatting.BOLD + dateString, poseStack, bufferSource, fontRender, 20, textColor, scaleForDate, light);
    Date date = new Date(state.deathDate);
    String fdateString = new SimpleDateFormat(DATE_FORMAT).format(date);
    String timeString = new SimpleDateFormat(TIME_FORMAT).format(date);
    showString(ChatFormatting.BOLD + fdateString, poseStack, bufferSource, fontRender, 36, textColor, scaleForDate, light);
    showString(ChatFormatting.BOLD + timeString, poseStack, bufferSource, fontRender, 46, textColor, scaleForDate, light);
    bufferSource.endBatch();
    poseStack.popPose();
  }

  private void showString(String content, PoseStack poseStack, MultiBufferSource bufferSource, Font font, int posY, int color, float scale, int light) {
    poseStack.pushPose();
    poseStack.scale(scale, scale, scale);
    font.drawInBatch(content, (float) -font.width(content) / 2, posY - 30, color, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);
    poseStack.popPose();
  }

  @Override
  public AABB getRenderBoundingBox(BlockEntityTomb blockEntity) {
    double renderExtension = 1.0D;
    return new AABB(
        blockEntity.getBlockPos().getX() - renderExtension,
        blockEntity.getBlockPos().getY() - renderExtension,
        blockEntity.getBlockPos().getZ() - renderExtension,
        blockEntity.getBlockPos().getX() + 1 + renderExtension,
        blockEntity.getBlockPos().getY() + 1 + renderExtension,
        blockEntity.getBlockPos().getZ() + 1 + renderExtension);
  }
}
