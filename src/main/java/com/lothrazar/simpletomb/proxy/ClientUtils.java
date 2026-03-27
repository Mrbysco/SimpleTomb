package com.lothrazar.simpletomb.proxy;

import com.lothrazar.simpletomb.TombRegistry;
import com.lothrazar.simpletomb.block.RenderTomb;
import com.lothrazar.simpletomb.particle.ParticleCasting;
import com.lothrazar.simpletomb.particle.ParticleGraveSmoke;
import com.lothrazar.simpletomb.particle.ParticleGraveSoul;
import com.lothrazar.simpletomb.particle.ParticleRotatingSmoke;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

import java.util.function.Predicate;

public class ClientUtils {

  private static SpriteSet castingSpriteSet;

  public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
    event.registerSpriteSet(TombRegistry.GRAVE_SMOKE.get(), ParticleGraveSmoke.Factory::new);
    event.registerSpriteSet(TombRegistry.ROTATING_SMOKE.get(), ParticleRotatingSmoke.Factory::new);
    event.registerSpriteSet(TombRegistry.SOUL.get(), ParticleGraveSoul.Factory::new);
    event.registerSpriteSet(TombRegistry.CASTING.get(), sprites -> {
      castingSpriteSet = sprites;
      return (type, level, x, y, z, mx, my, mz, random) -> null;
    });
  }

  public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(TombRegistry.TOMBSTONE_BLOCK_ENTITY.get(), RenderTomb::new);
  }

  public static void produceGraveSmoke(Level level, double x, double y, double z) {
    Minecraft.getInstance().particleEngine.createParticle(TombRegistry.GRAVE_SMOKE.get(),
            x + level.getRandom().nextGaussian(), y, z + level.getRandom().nextGaussian(),
            0d, 0d, 0d);
  }

  public static void produceGraveSoul(Level level, BlockPos pos) {
    level.addParticle(TombRegistry.SOUL.get(), pos.getX(), pos.getY(), pos.getZ(), 0d, 0d, 0d);
  }

  public static void produceParticleCasting(LivingEntity caster, Predicate<LivingEntity> predic) {
    Minecraft mc = Minecraft.getInstance();
    if (caster != null && caster.level() instanceof ClientLevel cworld && castingSpriteSet != null) {
      for (int i = 1; i <= 2; i++) {
        mc.particleEngine.add(new ParticleCasting(cworld, caster, predic, 0d, i * 0.5d, castingSpriteSet));
        mc.particleEngine.add(new ParticleCasting(cworld, caster, predic, 0.5d, (i + 1) * 0.5d, castingSpriteSet));
        mc.particleEngine.add(new ParticleCasting(cworld, caster, predic, 1d, i * 0.5d, castingSpriteSet));
        mc.particleEngine.add(new ParticleCasting(cworld, caster, predic, 1.5d, (i + 1) * 0.5d, castingSpriteSet));
        mc.particleEngine.add(new ParticleCasting(cworld, caster, predic, 2d, i * 0.5d, castingSpriteSet));
      }
    }
  }

  public static BlockPos getPlayerPos() {
    Minecraft mc = Minecraft.getInstance();
    if (mc.player == null) return null;
    return mc.player.blockPosition();
  }
}
