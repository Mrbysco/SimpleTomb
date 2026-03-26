package com.lothrazar.simpletomb.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;

public class TransparentParticle extends SingleQuadParticle {

  protected TransparentParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet) {
    super(level, x, y, z, spriteSet.get(0, 1));
  }

  @Override
  protected SingleQuadParticle.Layer getLayer() {
    return SingleQuadParticle.Layer.TRANSLUCENT;
  }
}
