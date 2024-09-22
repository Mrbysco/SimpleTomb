package com.lothrazar.simpletomb;

import com.lothrazar.simpletomb.block.BlockEntityTomb;
import com.lothrazar.simpletomb.block.BlockTomb;
import com.lothrazar.simpletomb.block.ModelTomb;
import com.lothrazar.simpletomb.item.GraveKeyItem;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TombRegistry {

  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModTomb.MODID);
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ModTomb.MODID);
  public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModTomb.MODID);
  public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, ModTomb.MODID);
  //Blocks
  public static final DeferredBlock<BlockTomb> GRAVE_SIMPLE = BLOCKS.register("grave_simple", () -> new BlockTomb(Block.Properties.of().mapColor(MapColor.STONE), ModelTomb.GRAVE_SIMPLE));
  public static final DeferredBlock<BlockTomb> GRAVE_NORMAL = BLOCKS.register("grave_normal", () -> new BlockTomb(Block.Properties.of().mapColor(MapColor.STONE), ModelTomb.GRAVE_NORMAL));
  public static final DeferredBlock<BlockTomb> GRAVE_CROSS = BLOCKS.register("grave_cross", () -> new BlockTomb(Block.Properties.of().mapColor(MapColor.STONE), ModelTomb.GRAVE_CROSS));
  public static final DeferredBlock<BlockTomb> TOMBSTONE = BLOCKS.register("tombstone", () -> new BlockTomb(Block.Properties.of().mapColor(MapColor.STONE), ModelTomb.GRAVE_TOMB));
  //Items
  public static final DeferredItem<GraveKeyItem> GRAVE_KEY = ITEMS.register("grave_key", () -> new GraveKeyItem(new Item.Properties()));
  //BlockEntities
  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityTomb>> TOMBSTONE_BLOCK_ENTITY = BLOCK_ENTITIES.register("tombstone", () -> BlockEntityType.Builder.of(BlockEntityTomb::new,
      TombRegistry.GRAVE_SIMPLE.get(),
      TombRegistry.GRAVE_NORMAL.get(),
      TombRegistry.GRAVE_CROSS.get(),
      TombRegistry.TOMBSTONE.get())
      .build(null));
  //Particles
  public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GRAVE_SMOKE = PARTICLE_TYPES.register("grave_smoke", () -> new SimpleParticleType(false));
  public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ROTATING_SMOKE = PARTICLE_TYPES.register("rotating_smoke", () -> new SimpleParticleType(false));
  public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SOUL = PARTICLE_TYPES.register("soul", () -> new SimpleParticleType(false));

  public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TOMBSTONE_BLOCK_ENTITY.get(), BlockEntityTomb::getHandler);
  }
}
