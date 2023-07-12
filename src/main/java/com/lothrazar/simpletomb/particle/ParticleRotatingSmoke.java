package com.lothrazar.simpletomb.particle;

import com.lothrazar.library.particle.TransparentParticle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class ParticleRotatingSmoke extends TransparentParticle {

  private final SpriteSet spriteSet;
  private final float rotIncrement;

  private ParticleRotatingSmoke(SpriteSet spriteSet, ClientLevel world, double x, double y, double z) {
    super(world, x, y + 0.3d, z);
    this.xd = this.yd = this.zd = 0d;
    setAlpha(0.5f);
    scale(2f);
    //    this.mul
    setLifetime(100);
    this.hasPhysics = false;
    this.oRoll = this.roll = (float) (world.random.nextFloat() * Math.PI * 2f);
    this.rotIncrement = (float) (Math.PI * 0.02f);
    setColor(0.7f, 0.7f, 0.7f);
    this.spriteSet = spriteSet;
    setSpriteFromAge(this.spriteSet);
  }

  private void updatePosition() {
    this.oRoll = this.roll;
    this.roll += this.rotIncrement;
    float color = 0.6f + this.level.random.nextFloat() * 0.2f;
    setColor(color, color, color);
  }

  @Override
  public void tick() {
    super.tick();
    if (isAlive()) {
      updatePosition();
      setSpriteFromAge(this.spriteSet);
    }
  }

  @Override
  protected int getLightColor(float partialTick) {
    int skylight = 15;
    int blocklight = 15;
    return skylight << 20 | blocklight << 4;
  }

  @Override
  public ParticleRenderType getRenderType() {
    return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
  }

  public static class Factory implements ParticleProvider<SimpleParticleType> {

    private SpriteSet spriteSet;

    public Factory(SpriteSet spriteSet) {
      this.spriteSet = spriteSet;
    }

    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ) {
      return new ParticleRotatingSmoke(this.spriteSet, world, x, y, z);
    }
  }
}
