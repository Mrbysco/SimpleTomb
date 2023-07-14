package com.lothrazar.simpletomb.event;

import com.lothrazar.library.core.BlockPosDim;
import com.lothrazar.library.util.LevelWorldUtil;
import com.lothrazar.library.util.RenderUtil;
import com.lothrazar.simpletomb.ModTomb;
import com.lothrazar.simpletomb.TombRegistry;
import com.lothrazar.simpletomb.particle.ParticleGraveSmoke;
import com.lothrazar.simpletomb.particle.ParticleGraveSoul;
import com.lothrazar.simpletomb.particle.ParticleRotatingSmoke;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModTomb.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
@OnlyIn(Dist.CLIENT)
public class ClientEvents {

  @SubscribeEvent(priority = EventPriority.LOW)
  public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
    //    ParticleEngine r = Minecraft.getInstance().particleEngine;
    event.registerSpriteSet(TombRegistry.GRAVE_SMOKE.get(), ParticleGraveSmoke.Factory::new);
    event.registerSpriteSet(TombRegistry.ROTATING_SMOKE.get(), ParticleRotatingSmoke.Factory::new);
    event.registerSpriteSet(TombRegistry.SOUL.get(), ParticleGraveSoul.Factory::new);
    //    r.register(TombRegistry.GRAVE_SMOKE.get(), ParticleGraveSmoke.Factory::new);
    //    r.register(TombRegistry.ROTATING_SMOKE.get(), ParticleRotatingSmoke.Factory::new);
    //    r.register(TombRegistry.SOUL.get(), ParticleGraveSoul.Factory::new);
  }

  @SubscribeEvent
  public void render(RenderLevelStageEvent event) {
    if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && player.level() != null) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() == TombRegistry.GRAVE_KEY.get()) {
          MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
          BlockPosDim location = TombRegistry.GRAVE_KEY.get().getTombPos(stack);
          if (location != null && !location.isOrigin() &&
              location.getDimension().equalsIgnoreCase(LevelWorldUtil.dimensionToString(player.level())) &&
              player.level().isInWorldBounds(location.toBlockPos())) {
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            RenderUtil.createBox(bufferSource, poseStack, location.getX(), (float) location.getY(), location.getZ(), 1.0F);
            poseStack.popPose();
          }
        }
      }
    }
  }
}
