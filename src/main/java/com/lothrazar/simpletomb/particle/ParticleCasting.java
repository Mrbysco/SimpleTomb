package com.lothrazar.simpletomb.particle;

import com.lothrazar.simpletomb.ModTomb;
import com.lothrazar.simpletomb.helper.WorldHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Predicate;

public class ParticleCasting extends CustomParticle {

  private static final ResourceLocation COMMON_TEXTURE = ResourceLocation.fromNamespaceAndPath(ModTomb.MODID, "textures/particle/casting.png");
  private final LivingEntity caster;
  private final Predicate<LivingEntity> predic;
  private final double radius = 1.1;
  private double angle;
  private static final double ROT_INCR = Math.PI * 0.05D;
  private final float colorR;
  private final float colorG;
  private final float colorB;
  private boolean goUp;

  public ParticleCasting(ClientLevel level, LivingEntity caster, Predicate<LivingEntity> predic, double addY, double angle) {
    super(level, caster.getX(), caster.getY() + addY, caster.getZ());
    this.xd = this.yd = this.zd = 0d;
    setAlpha(1f);
    this.goUp = addY < 1d;
    this.caster = caster;
    this.predic = predic;
    this.quadSize = level.random.nextFloat() * 0.1f + 0.15f;
    this.angle = angle + WorldHelper.getRandom(level.random, -0.25, 0.25);
    this.roll = level.random.nextFloat() * (float) (2d * Math.PI);
    float[] color = WorldHelper.getRGBColor3F(14937088);
    this.colorR = color[0];
    this.colorG = color[1];
    this.colorB = color[2];
    this.hasPhysics = false;
    updatePosition();
  }

  private void updatePosition() {
    this.angle += 0.01f;
    this.xo = this.x = caster.getX() + this.radius * Math.cos(2 * Math.PI * (this.angle));
    this.yo = this.y = this.y + (this.goUp ? 0.02d : -0.02d);
    this.zo = this.z = caster.getZ() + this.radius * Math.sin(2 * Math.PI * (this.angle));
    setColor(clampColor(this.colorR + (WorldHelper.getRandom(level.random, -20f, 20f) / 255f)), clampColor(this.colorG - (WorldHelper.getRandom(level.random, -20f, 20f) / 255f)),
        clampColor(this.colorB + (WorldHelper.getRandom(level.random, -20f, 20f) / 255f)));
    this.oRoll = this.roll;
    this.roll += (float) ROT_INCR;
  }

  private float clampColor(float color) {
    return Mth.clamp(color, 0f, 1f);
  }

  @Override
  public void tick() {
    if (this.y > caster.getY() + 2d || this.y < caster.getY()) {
      this.goUp = !this.goUp;
    }
    if (this.predic.test(this.caster)) {
      remove();
    }
    updatePosition();
    this.age++;
  }

  @Override
  protected int getLightColor(float partialTick) {
    int skylight = 5;
    int blocklight = 15;
    return skylight << 20 | blocklight << 4;
  }

  @Override
  ResourceLocation getTexture() {
    return COMMON_TEXTURE;
  }
}
