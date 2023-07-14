package com.lothrazar.simpletomb.particle;

import com.lothrazar.library.particle.TransparentParticle;
import com.lothrazar.simpletomb.helper.WorldHelper;
import com.lothrazar.simpletomb.proxy.ClientUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParticleGhost extends TransparentParticle {

  private final SpriteSet spriteSet;
  private final double mX, mZ;

  private ParticleGhost(SpriteSet spriteSet, ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ) {
    super(world, x, y + 1d, z);
    this.mX = motionX;
    this.mZ = motionZ;
    this.xd = this.yd = this.zd = 0d;
    setLifetime(200);
    this.hasPhysics = false;
    scale(8f);
    setColor(1f, 1f, 1f);
    this.spriteSet = spriteSet;
    setSpriteFromAge(this.spriteSet);
  }

  @Override
  public void tick() {
    super.tick();
    if (isAlive()) {
      if (this.age == 10) {
        this.xd = mX;
        this.zd = mZ;
      }
      float ratio = this.age / (float) this.lifetime;
      setAlpha((1f - ratio) * 0.8f);
      setSpriteFromAge(this.spriteSet);
      if (level.isClientSide) {
        ClientUtils.produceGraveSmoke(this.level, this.x, this.y - 1d, this.z);
      }
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
      RandomSource rand = (world == null) ? RandomSource.create(0) : world.random;
      return new ParticleGhost(this.spriteSet, world, x, y, z, WorldHelper.getRandom(rand, -0.05d, 0.05d), 0d, WorldHelper.getRandom(rand, -0.05d, 0.05d));
    }
  }
}
