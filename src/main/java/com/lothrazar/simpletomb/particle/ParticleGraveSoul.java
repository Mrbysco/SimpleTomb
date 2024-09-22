package com.lothrazar.simpletomb.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;


public class ParticleGraveSoul extends TextureSheetParticle {

  private final SpriteSet spriteSet;
  private final double radius, centerX, centerZ;

  private ParticleGraveSoul(SpriteSet spriteSet, ClientLevel world, double x, double y, double z, double radius) {
    super(world, x, y + 0.85d, z);
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
  public ParticleRenderType getRenderType() {
    return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
  }

  public static class Factory implements ParticleProvider<SimpleParticleType> {

    private SpriteSet spriteSet;

    public Factory(SpriteSet spriteSet) {
      this.spriteSet = spriteSet;
    }

    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ) {
      return new ParticleGraveSoul(this.spriteSet, world, x, y, z, 0.3d);
    }
  }
}
