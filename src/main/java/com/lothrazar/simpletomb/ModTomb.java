package com.lothrazar.simpletomb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lothrazar.simpletomb.event.CommandEvents;
import com.lothrazar.simpletomb.event.PlayerTombEvents;
import com.lothrazar.simpletomb.proxy.ClientUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ModTomb.MODID)
public class ModTomb {

  public static final PlayerTombEvents GLOBAL = new PlayerTombEvents();
  public static final String MODID = "simpletomb";
  public static final Logger LOGGER = LogManager.getLogger();

  public ModTomb() {
    new ConfigTomb();
    IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
    eventBus.addListener(this::setup);
    TombRegistry.BLOCKS.register(eventBus);
    TombRegistry.ITEMS.register(eventBus);
    TombRegistry.BLOCK_ENTITIES.register(eventBus);
    TombRegistry.PARTICLE_TYPES.register(eventBus);
    MinecraftForge.EVENT_BUS.register(new CommandEvents());
    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
      eventBus.addListener(ClientUtils::onClientSetup);
      eventBus.addListener(ClientUtils::registerEntityRenders);
    });
  }

  private void setup(final FMLCommonSetupEvent event) {
    MinecraftForge.EVENT_BUS.register(GLOBAL);
  }
}
