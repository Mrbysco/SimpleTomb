package com.lothrazar.simpletomb.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class ParticleGraveSoul extends SingleQuadParticle {

  private final SpriteSet spriteSet;
  private final double radius, centerX, centerZ;

  private ParticleGraveSoul(SpriteSet spriteSet, ClientLevel level, double x, double y, double z, double radius) {
    super(level, x, y + 0.85d, z, spriteSet.get(0, 1));
    this.lifetime = 100;
    this.quadSize = 0.03f;
    this.centerX = x + 0.5d;
    this.centerZ = z + 0.5d;
    this.radius = radius;
    updatePosition();
    setAlpha(0.7f);
    setColor(81f / 255f, 25f / 255f, 139f / 255f);
    this.hasPhysics = false;
    this.spriteSet = spriteSet;
    setSpriteFromAge(this.spriteSet);
  }

  private void updatePosition() {
    double ratio = this.age / (double) this.lifetime;
    this.xd = this.yd = this.zd = 0d;
    this.xo = this.x = this.centerX + this.radius * Math.cos(2 * Math.PI * ratio);
    this.yo = this.y;
    this.zo = this.z = this.centerZ + this.radius * Math.sin(2 * Math.PI * ratio);
  }

  @Override
  public void tick() {
    super.tick();
    if (isAlive()) {
      setSpriteFromAge(this.spriteSet);
      updatePosition();
    }
  }

  @Override
  protected SingleQuadParticle.Layer getLayer() {
    return SingleQuadParticle.Layer.TRANSLUCENT;
  }

  public static class Factory implements ParticleProvider<SimpleParticleType> {

    private final SpriteSet spriteSet;

    public Factory(SpriteSet spriteSet) {
      this.spriteSet = spriteSet;
    }

    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ, RandomSource random) {
      return new ParticleGraveSoul(this.spriteSet, level, x, y, z, 0.3d);
    }
  }
}
