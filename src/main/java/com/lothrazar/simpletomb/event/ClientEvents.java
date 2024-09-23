package com.lothrazar.simpletomb.event;

import com.lothrazar.simpletomb.TombRegistry;
import com.lothrazar.simpletomb.client.LineRenderType;
import com.lothrazar.simpletomb.data.DeathHelper;
import com.lothrazar.simpletomb.helper.WorldHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public class ClientEvents {

  @SubscribeEvent
  public static void renderEvent(RenderLevelStageEvent event) {
    if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && player.level() != null) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() == TombRegistry.GRAVE_KEY.get()) {
          MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
          GlobalPos location = TombRegistry.GRAVE_KEY.get().getTombPos(stack);
          if (location != null && !location.equals(DeathHelper.ORIGIN) &&
              location.dimension().equals(player.level().dimension()) &&
              player.level().isInWorldBounds(location.pos())) {
            BlockPos tombPos = location.pos();
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            createBox(bufferSource, poseStack, tombPos.getX(), (float) tombPos.getY(), tombPos.getZ(), 1.0F);
            poseStack.popPose();
          }
        }
      }
    }
  }

  public static void createBox(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, float x, float y, float z, float offset) {
    long c = (System.currentTimeMillis() / 15L) % 360L;
    float[] color = WorldHelper.getHSBtoRGBF(c / 360f, 1f, 1f);
    Minecraft mc = Minecraft.getInstance();
    Vec3 cameraPosition = mc.gameRenderer.getMainCamera().getPosition();
    // get a closer pos if too far
    Vec3 vec = new Vec3(x, y, z).subtract(cameraPosition);
    if (vec.distanceTo(Vec3.ZERO) > 200d) { // could be 300
      vec = vec.normalize().scale(200d);
      x += (float) vec.x;
      y += (float) vec.y;
      z += (float) vec.z;
    }
    RenderSystem.disableDepthTest();
    RenderType renderType = LineRenderType.tombLinesType();
    VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
    poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
    Matrix4f pose = poseStack.last().pose();
    vertexConsumer.addVertex(pose, x, y, z).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x + offset, y, z).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x, y, z).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x, y + offset, z).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x, y, z).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x, y, z + offset).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x + offset, y + offset, z + offset).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x, y + offset, z + offset).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x + offset, y + offset, z + offset).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x + offset, y, z + offset).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x + offset, y + offset, z + offset).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x + offset, y + offset, z).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x, y + offset, z).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x, y + offset, z + offset).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x, y + offset, z).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x + offset, y + offset, z).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x + offset, y, z).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x + offset, y, z + offset).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x + offset, y, z).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x + offset, y + offset, z).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x, y, z + offset).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x + offset, y, z + offset).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x, y, z + offset).setColor(color[0], color[1], color[2], 1.0F);
    vertexConsumer.addVertex(pose, x, y + offset, z + offset).setColor(color[0], color[1], color[2], 1.0F);
    bufferSource.endBatch(renderType);
    RenderSystem.enableDepthTest();
  }
}
