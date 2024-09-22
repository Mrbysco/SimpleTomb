package com.lothrazar.simpletomb.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;

public class TransparentParticle extends TextureSheetParticle {

  protected TransparentParticle(ClientLevel world, double x, double y, double z) {
    super(world, x, y, z);
  }

  @Override
  public ParticleRenderType getRenderType() {
    return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
  }

  @Override
  public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
    RenderSystem.depthMask(false);
    super.render(buffer, renderInfo, partialTicks);
  }
}
