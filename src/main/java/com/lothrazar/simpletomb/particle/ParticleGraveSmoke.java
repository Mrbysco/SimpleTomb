package com.lothrazar.simpletomb.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.Random;


public class ParticleGraveSmoke extends TransparentParticle {

  final Random rand = new Random();
  private final SpriteSet spriteSet;
  protected final int halfMaxAge;
  protected final float alphaStep;
  private final float rotIncrement;

  private ParticleGraveSmoke(SpriteSet spriteSet, ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ) {
    super(level, x, y + 0.1d, z);
    this.xd = motionX;
    this.yd = motionY;
    this.zd = motionZ;
    this.alpha = 0f;
    scale(4f);
    this.roll = (float) (Math.PI * 2) * rand.nextFloat();
    this.rotIncrement = (float) (Math.PI * (rand.nextFloat() - 0.5f) * 0.01d);
    setLifetime(80);
    this.halfMaxAge = this.lifetime / 2;
    this.alphaStep = 0.08f / this.halfMaxAge;
    this.hasPhysics = false;
    setColor(0, .5F, .1F);
    //    
    this.spriteSet = spriteSet;
    setSpriteFromAge(this.spriteSet);
  }

  @Override
  public void tick() {
    super.tick();
    if (isAlive()) {
      setSpriteFromAge(this.spriteSet);
      this.oRoll = this.roll;
      this.roll += rotIncrement;
      setAlpha(Mth.clamp(this.age < this.halfMaxAge ? this.age : this.lifetime - this.age, 0, this.halfMaxAge) * this.alphaStep);
    }
  }

  @Override
  protected int getLightColor(float partialTick) {
    int skylight = 8;
    int blocklight = 15;
    return skylight << 20 | blocklight << 4;
  }

  @Override
  public ParticleRenderType getRenderType() {
    return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
  }

  public static class Factory implements ParticleProvider<SimpleParticleType> {

    private final SpriteSet spriteSet;

    public Factory(SpriteSet spriteSet) {
      this.spriteSet = spriteSet;
    }

    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ) {
      RandomSource rand = (level == null) ? RandomSource.createThreadSafe() : level.random;
      return new ParticleGraveSmoke(this.spriteSet, level, x, y + 0.4d, z, (rand.nextFloat() - 0.5f) * 0.03d, 0d, (rand.nextFloat() - 0.5f) * 0.03d);
    }
  }
}
