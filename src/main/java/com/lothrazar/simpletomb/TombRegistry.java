package com.lothrazar.simpletomb;

import com.lothrazar.simpletomb.block.BlockEntityTomb;
import com.lothrazar.simpletomb.block.BlockTomb;
import com.lothrazar.simpletomb.block.ModelTomb;
import com.lothrazar.simpletomb.item.GraveKeyItem;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TombRegistry {

  public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ModTomb.MODID);
  public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModTomb.MODID);
  public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ModTomb.MODID);
  public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ModTomb.MODID);
  //Blocks
  public static final RegistryObject<BlockTomb> GRAVE_SIMPLE = BLOCKS.register("grave_simple", () -> new BlockTomb(Block.Properties.of().mapColor(MapColor.STONE), ModelTomb.GRAVE_SIMPLE));
  public static final RegistryObject<BlockTomb> GRAVE_NORMAL = BLOCKS.register("grave_normal", () -> new BlockTomb(Block.Properties.of().mapColor(MapColor.STONE), ModelTomb.GRAVE_NORMAL));
  public static final RegistryObject<BlockTomb> GRAVE_CROSS = BLOCKS.register("grave_cross", () -> new BlockTomb(Block.Properties.of().mapColor(MapColor.STONE), ModelTomb.GRAVE_CROSS));
  public static final RegistryObject<BlockTomb> TOMBSTONE = BLOCKS.register("tombstone", () -> new BlockTomb(Block.Properties.of().mapColor(MapColor.STONE), ModelTomb.GRAVE_TOMB));
  //Items
  public static final RegistryObject<GraveKeyItem> GRAVE_KEY = ITEMS.register("grave_key", () -> new GraveKeyItem(new Item.Properties()));
  //BlockEntities
  public static final RegistryObject<BlockEntityType<BlockEntityTomb>> TOMBSTONE_BLOCK_ENTITY = BLOCK_ENTITIES.register("tombstone", () -> BlockEntityType.Builder.of(BlockEntityTomb::new,
      TombRegistry.GRAVE_SIMPLE.get(),
      TombRegistry.GRAVE_NORMAL.get(),
      TombRegistry.GRAVE_CROSS.get(),
      TombRegistry.TOMBSTONE.get())
      .build(null));
  //Particles
  public static final RegistryObject<SimpleParticleType> GRAVE_SMOKE = PARTICLE_TYPES.register("grave_smoke", () -> new SimpleParticleType(false));
  public static final RegistryObject<SimpleParticleType> ROTATING_SMOKE = PARTICLE_TYPES.register("rotating_smoke", () -> new SimpleParticleType(false));
  public static final RegistryObject<SimpleParticleType> SOUL = PARTICLE_TYPES.register("soul", () -> new SimpleParticleType(false));
}
