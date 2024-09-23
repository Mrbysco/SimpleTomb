package com.lothrazar.simpletomb.block;

import com.lothrazar.simpletomb.data.MessageType;
import com.lothrazar.simpletomb.helper.WorldHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.text.SimpleDateFormat;
import java.util.Date;


public class RenderTomb implements BlockEntityRenderer<BlockEntityTomb> {

  private static final ResourceLocation SKELETON_HEAD = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png");
  private final Font font;

  public RenderTomb(BlockEntityRendererProvider.Context cx) {
    this.font = cx.getFont();
  }

  private static final String TIME_FORMAT = "HH:mm:ss";
  private static final String DATE_FORMAT = "yyyy/MM/dd";

  @Override
  public void render(BlockEntityTomb te, float partialTicks, PoseStack poseStack,
      MultiBufferSource bufferSource, int light, int destroyStage) {
    if (te == null) {
      return;
    }
    if (!te.hasOwner()) {
      return;
    }
    BlockState knownState = te.getLevel().getBlockState(te.getBlockPos());
    if (!(knownState.getBlock() instanceof BlockTomb grave)) {
      return;
    }
    Direction facing = knownState.getValue(BlockTomb.FACING);
    ModelTomb graveModel = grave.getGraveType();
    renderHalloween(poseStack, bufferSource, graveModel, facing, light, WorldHelper.isNight(te.getLevel()));
    light = 0xf000f0;
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
    poseStack.mulPose(Axis.YP.rotationDegrees(-90f * rotationIndex)); // horizontal rot
    Font fontRender = this.font;
    int textColor = 0xFFFFFFFF;
    // rip message
    showString(ChatFormatting.BOLD + MessageType.MESSAGE_RIP.getTranslation(), poseStack, bufferSource, fontRender, 0,
        textColor, 0.007f, light);
    // owner message
    showString(ChatFormatting.BOLD + te.getOwnerName(), poseStack, bufferSource, fontRender, 11, textColor, 0.005f, light);
    // death date message
    float scaleForDate = 0.004f;
    // time goes 72 times faster than real time
    long days = te.timer / 24000; // TODO incorrect, tiles don't always tick, store gametime
    String dateString = MessageType.MESSAGE_DAY.getTranslation(days);
    showString(ChatFormatting.BOLD + dateString, poseStack, bufferSource, fontRender, 20, textColor, scaleForDate, light);
    Date date = new Date(te.getOwnerDeathTime());
    String fdateString = new SimpleDateFormat(DATE_FORMAT).format(date);
    String timeString = new SimpleDateFormat(TIME_FORMAT).format(date);
    showString(ChatFormatting.BOLD + fdateString, poseStack, bufferSource, fontRender, 36, textColor, scaleForDate, light);
    showString(ChatFormatting.BOLD + timeString, poseStack, bufferSource, fontRender, 46, textColor, scaleForDate, light);
    poseStack.popPose();
  }

  private void showString(String content, PoseStack poseStack, MultiBufferSource bufferSource, Font font, int posY, int color, float scale, int light) {
    poseStack.pushPose();
    poseStack.scale(scale, scale, scale);
    font.drawInBatch(content, (float) -font.width(content) / 2, posY - 30, color, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);
    poseStack.popPose();
  }

  private void renderHalloween(PoseStack poseStack, MultiBufferSource bufferSource, ModelTomb graveModel, Direction facing, int light, boolean isNight) {
    //    RenderSystem.enableRescaleNormal();
    RenderSystem.disableCull();
    //    RenderSystem.enableAlphaTest();
    float decoX = 0.5f, decoY = 0.07f, decoZ = 0.5f;
    switch (graveModel) {
      case GRAVE_NORMAL:
        decoY += 0.35f;
      break;
      case GRAVE_CROSS:
        if (facing == Direction.SOUTH) {
          decoX -= 0.2f;
        }
        else if (facing == Direction.WEST) {
          decoZ -= 0.2f;
        }
        else if (facing == Direction.EAST) {
          decoZ += 0.2f;
        }
        else {
          decoX += 0.2f;
        }
      break;
      case GRAVE_SIMPLE:
      default:
        decoY += 0.1f;
      break;
    }
    Minecraft.getInstance().getTextureManager().bindForSetup(SKELETON_HEAD);
    poseStack.pushPose();
    poseStack.translate(decoX, decoY, decoZ);
    poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot() + (facing == Direction.SOUTH || facing == Direction.NORTH ? 180 : 0)));
    if (graveModel == ModelTomb.GRAVE_NORMAL || graveModel == ModelTomb.GRAVE_SIMPLE) {
      poseStack.scale(0.2f, 0.2f, 0.2f);
      ItemStack stack = new ItemStack(isNight ? Blocks.JACK_O_LANTERN : Blocks.PUMPKIN);
      Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.NONE, false, poseStack, bufferSource, 15728880,
          net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, 0));
    }
    else {
      poseStack.scale(0.3f, 0.3f, 0.3f);
      //      SkullBlock.Type skullblock$type = ((AbstractSkullBlock)block).getType();
      //      SkullModelBase skullmodelbase = this.skullModels.get(skullblock$type);
      //      RenderType rendertype = SkullBlockRenderer.getRenderType(skullblock$type, gameprofile);
      //      SkullBlockRenderer.renderSkull((Direction)null, 180.0F, 0.0F, p_108832_, p_108833_, p_108834_, skullmodelbase, rendertype);
      // TODO:::
      //      SkullBlockRenderer.renderSkull(null, 1f, SkullBlock.Types.SKELETON, null, 0f, matrixStack, iRenderTypeBuffer, isNight ? 0xf000f0 : light);
    }
    poseStack.popPose();
    //    RenderSystem.popMatrix();
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
