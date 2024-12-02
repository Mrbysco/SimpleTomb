package com.lothrazar.simpletomb;

import com.lothrazar.simpletomb.event.ClientEvents;
import com.lothrazar.simpletomb.event.CommandEvents;
import com.lothrazar.simpletomb.event.PlayerTombEvents;
import com.lothrazar.simpletomb.proxy.ClientUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ModTomb.MODID)
public class ModTomb {

  public static final PlayerTombEvents GLOBAL = new PlayerTombEvents();
  public static final String MODID = "simpletomb";
  public static final Logger LOGGER = LogManager.getLogger();

  public ModTomb(IEventBus eventBus, Dist dist, ModContainer container) {
    container.registerConfig(ModConfig.Type.COMMON, ConfigTomb.CONFIG, MODID + ".toml");
    eventBus.addListener(this::setup);
    TombComponents.COMPONENT_TYPE.register(eventBus);
    TombRegistry.BLOCKS.register(eventBus);
    TombRegistry.ITEMS.register(eventBus);
    TombRegistry.BLOCK_ENTITIES.register(eventBus);
    TombRegistry.PARTICLE_TYPES.register(eventBus);
    eventBus.addListener(TombRegistry::registerCapabilities);
    NeoForge.EVENT_BUS.register(new CommandEvents());
    if (dist.isClient()) {
      container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
      eventBus.addListener(ClientUtils::registerEntityRenders);
      eventBus.addListener(ClientUtils::registerParticleFactories);
      NeoForge.EVENT_BUS.addListener(ClientEvents::renderEvent);
    }
  }

  private void setup(final FMLCommonSetupEvent event) {
    NeoForge.EVENT_BUS.register(GLOBAL);
  }
}
