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
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public class ClientEvents {

  @SubscribeEvent
  public static void renderEvent(RenderLevelStageEvent.AfterTranslucentBlocks event) {
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

  public static void createBox(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, float x, float y, float z, float offset) {
    long c = (System.currentTimeMillis() / 15L) % 360L;
    float[] color = WorldHelper.getHSBtoRGBF(c / 360f, 1f, 1f);
    Minecraft mc = Minecraft.getInstance();
    Vec3 cameraPosition = mc.gameRenderer.getMainCamera().position();
    Vec3 vec = new Vec3(x, y, z).subtract(cameraPosition);
    if (vec.distanceTo(Vec3.ZERO) > 200d) {
      vec = vec.normalize().scale(200d);
      x += (float) vec.x;
      y += (float) vec.y;
      z += (float) vec.z;
    }
    RenderType renderType = LineRenderType.tombLinesType();
    VertexConsumer vc = bufferSource.getBuffer(renderType);
    poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
    Matrix4f pose = poseStack.last().pose();
    int r = (int) (color[0] * 255), g = (int) (color[1] * 255), b = (int) (color[2] * 255);
    line(vc, pose, x, y, z, x + offset, y, z, r, g, b);
    line(vc, pose, x, y, z, x, y + offset, z, r, g, b);
    line(vc, pose, x, y, z, x, y, z + offset, r, g, b);
    line(vc, pose, x + offset, y + offset, z + offset, x, y + offset, z + offset, r, g, b);
    line(vc, pose, x + offset, y + offset, z + offset, x + offset, y, z + offset, r, g, b);
    line(vc, pose, x + offset, y + offset, z + offset, x + offset, y + offset, z, r, g, b);
    line(vc, pose, x, y + offset, z, x, y + offset, z + offset, r, g, b);
    line(vc, pose, x, y + offset, z, x + offset, y + offset, z, r, g, b);
    line(vc, pose, x + offset, y, z, x + offset, y, z + offset, r, g, b);
    line(vc, pose, x + offset, y, z, x + offset, y + offset, z, r, g, b);
    line(vc, pose, x, y, z + offset, x + offset, y, z + offset, r, g, b);
    line(vc, pose, x, y, z + offset, x, y + offset, z + offset, r, g, b);
    bufferSource.endBatch(renderType);
  }

  private static void line(VertexConsumer vc, Matrix4f pose, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b) {
    float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
    float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    if (len < 1e-6f) len = 1f;
    float nx = dx / len, ny = dy / len, nz = dz / len;
    vc.addVertex(pose, x1, y1, z1).setColor(r, g, b, 255).setNormal(nx, ny, nz).setLineWidth(2.5f);
    vc.addVertex(pose, x2, y2, z2).setColor(r, g, b, 255).setNormal(nx, ny, nz).setLineWidth(2.5f);
  }
}
