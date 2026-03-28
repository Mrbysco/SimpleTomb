package com.lothrazar.simpletomb.test;

import com.lothrazar.simpletomb.TombRegistry;
import com.lothrazar.simpletomb.block.BlockEntityTomb;
import com.lothrazar.simpletomb.block.BlockTomb;
import com.lothrazar.simpletomb.helper.EntityHelper;
import com.lothrazar.simpletomb.helper.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;

public class SimpleTombTestFunctions {

  private static final BlockPos CENTER = new BlockPos(4, 1, 4);

  static ServerPlayer mockPlayer(GameTestHelper helper) {
    var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(
        new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "test-mock-player"), false);
    ServerPlayer player = new ServerPlayer(
        helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation()
    ) {
      @Override
      public net.minecraft.world.level.GameType gameMode() {
        return net.minecraft.world.level.GameType.SURVIVAL;
      }
    };
    var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
    new io.netty.channel.embedded.EmbeddedChannel(connection);
    try {
      helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
    }
    catch (UnsupportedOperationException ignored) {
    }
    player.getAbilities().instabuild = false;
    player.getAbilities().invulnerable = false;
    return player;
  }

  private static BlockState graveState(Direction facing) {
    return TombRegistry.GRAVE_SIMPLE.get().defaultBlockState()
        .setValue(BlockTomb.FACING, facing)
        .setValue(BlockTomb.IS_ENGRAVED, false)
        .setValue(BlockTomb.MODEL_TEXTURE, 0);
  }

  static void placeGraveWithFloor(GameTestHelper helper, BlockPos pos) {
    helper.setBlock(pos.below(), Blocks.STONE);
    helper.setBlock(pos, graveState(Direction.NORTH));
  }

  private static BlockEntityTomb getGrave(GameTestHelper helper, BlockPos relPos) {
    BlockPos absPos = helper.absolutePos(relPos);
    BlockEntityTomb tile = BlockTomb.getBlockEntity(helper.getLevel(), absPos);
    if (tile == null) {
      helper.fail("No BlockEntityTomb at " + relPos);
    }
    return tile;
  }

  // === Block Properties ===

  static void tombBlockPlacement(GameTestHelper helper) {
    BlockPos pos1 = new BlockPos(1, 1, 1);
    BlockPos pos2 = new BlockPos(3, 1, 1);
    BlockPos pos3 = new BlockPos(5, 1, 1);
    BlockPos pos4 = new BlockPos(7, 1, 1);

    helper.setBlock(pos1, TombRegistry.GRAVE_SIMPLE.get().defaultBlockState()
        .setValue(BlockTomb.FACING, Direction.NORTH).setValue(BlockTomb.IS_ENGRAVED, false).setValue(BlockTomb.MODEL_TEXTURE, 0));
    helper.setBlock(pos2, TombRegistry.GRAVE_NORMAL.get().defaultBlockState()
        .setValue(BlockTomb.FACING, Direction.SOUTH).setValue(BlockTomb.IS_ENGRAVED, false).setValue(BlockTomb.MODEL_TEXTURE, 0));
    helper.setBlock(pos3, TombRegistry.GRAVE_CROSS.get().defaultBlockState()
        .setValue(BlockTomb.FACING, Direction.EAST).setValue(BlockTomb.IS_ENGRAVED, false).setValue(BlockTomb.MODEL_TEXTURE, 0));
    helper.setBlock(pos4, TombRegistry.TOMBSTONE.get().defaultBlockState()
        .setValue(BlockTomb.FACING, Direction.WEST).setValue(BlockTomb.IS_ENGRAVED, false).setValue(BlockTomb.MODEL_TEXTURE, 0));

    helper.assertTrue(BlockTomb.getBlockEntity(helper.getLevel(), helper.absolutePos(pos1)) instanceof BlockEntityTomb, "GRAVE_SIMPLE should have BlockEntityTomb");
    helper.assertTrue(BlockTomb.getBlockEntity(helper.getLevel(), helper.absolutePos(pos2)) instanceof BlockEntityTomb, "GRAVE_NORMAL should have BlockEntityTomb");
    helper.assertTrue(BlockTomb.getBlockEntity(helper.getLevel(), helper.absolutePos(pos3)) instanceof BlockEntityTomb, "GRAVE_CROSS should have BlockEntityTomb");
    helper.assertTrue(BlockTomb.getBlockEntity(helper.getLevel(), helper.absolutePos(pos4)) instanceof BlockEntityTomb, "TOMBSTONE should have BlockEntityTomb");
    helper.succeed();
  }

  static void tombBlockExplosion(GameTestHelper helper) {
    placeGraveWithFloor(helper, CENTER);
    helper.assertBlockPresent(TombRegistry.GRAVE_SIMPLE.get(), CENTER);

    BlockPos absCenter = helper.absolutePos(CENTER);
    ServerLevel level = helper.getLevel();
    level.explode(null, absCenter.getX(), absCenter.getY(), absCenter.getZ(), 4.0F, Level.ExplosionInteraction.TNT);

    helper.runAfterDelay(5, () -> {
      helper.assertBlockPresent(TombRegistry.GRAVE_SIMPLE.get(), CENTER);
      helper.succeed();
    });
  }

  static void tombBlockShape(GameTestHelper helper) {
    placeGraveWithFloor(helper, CENTER);
    BlockPos absPos = helper.absolutePos(CENTER);
    BlockState state = helper.getLevel().getBlockState(absPos);
    var shape = state.getShape(helper.getLevel(), absPos);
    helper.assertTrue(shape.min(Direction.Axis.Y) == 0.0, "Shape min Y should be 0");
    helper.assertTrue(shape.max(Direction.Axis.Y) == 0.25, "Shape max Y should be 0.25 (4/16)");
    helper.succeed();
  }

  // === Block Entity ===

  static void blockEntityStoresItems(GameTestHelper helper) {
    placeGraveWithFloor(helper, CENTER);
    BlockEntityTomb tile = getGrave(helper, CENTER);
    ItemStacksResourceHandler handler = tile.getHandler(null);

    try (var tx = Transaction.openRoot()) {
      ResourceHandlerUtil.insertStacking(handler, ItemResource.of(new ItemStack(Items.DIAMOND)), 32, tx);
      ResourceHandlerUtil.insertStacking(handler, ItemResource.of(new ItemStack(Items.IRON_SWORD)), 1, tx);
      tx.commit();
    }

    boolean foundDiamond = false;
    boolean foundSword = false;
    for (int i = 0; i < handler.size(); i++) {
      ItemResource resource = handler.getResource(i);
      int amount = handler.getAmountAsInt(i);
      if (resource.toStack(1).is(Items.DIAMOND) && amount == 32) foundDiamond = true;
      if (resource.toStack(1).is(Items.IRON_SWORD) && amount == 1) foundSword = true;
    }
    helper.assertTrue(foundDiamond, "Handler should contain 32 diamonds");
    helper.assertTrue(foundSword, "Handler should contain 1 iron sword");
    helper.succeed();
  }

  @SuppressWarnings("removal")
  static void blockEntityOwnership(GameTestHelper helper) {
    placeGraveWithFloor(helper, CENTER);
    BlockEntityTomb tile = getGrave(helper, CENTER);

    ServerPlayer player1 = mockPlayer(helper);
    ServerPlayer player2 = mockPlayer(helper);

    tile.initTombstoneOwner(player1);
    helper.assertTrue(tile.isOwner(player1), "Player1 should be the owner");
    helper.assertFalse(tile.isOwner(player2), "Player2 should not be the owner");
    helper.assertFalse(tile.isOwner(null), "Null should not be the owner");
    helper.succeed();
  }

  @SuppressWarnings("removal")
  static void blockEntityDropInventory(GameTestHelper helper) {
    placeGraveWithFloor(helper, CENTER);
    BlockEntityTomb tile = getGrave(helper, CENTER);
    ItemStacksResourceHandler handler = tile.getHandler(null);

    try (var tx = Transaction.openRoot()) {
      ResourceHandlerUtil.insertStacking(handler, ItemResource.of(new ItemStack(Items.DIAMOND_SWORD)), 1, tx);
      ResourceHandlerUtil.insertStacking(handler, ItemResource.of(new ItemStack(Items.GOLDEN_APPLE)), 16, tx);
      tx.commit();
    }

    BlockPos absPos = helper.absolutePos(CENTER);
    tile.dropInventory(helper.getLevel(), absPos);

    helper.runAfterDelay(5, () -> {
      AABB area = new AABB(absPos).inflate(2);
      List<ItemEntity> items = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area);
      boolean hasSword = items.stream().anyMatch(e -> e.getItem().is(Items.DIAMOND_SWORD));
      boolean hasApple = items.stream().anyMatch(e -> e.getItem().is(Items.GOLDEN_APPLE));
      helper.assertTrue(hasSword, "Dropped items should include diamond sword");
      helper.assertTrue(hasApple, "Dropped items should include golden apple");
      helper.succeed();
    });
  }

  // === Grave Activation ===

  @SuppressWarnings("removal")
  static void graveActivationOwner(GameTestHelper helper) {
    placeGraveWithFloor(helper, CENTER);
    BlockEntityTomb tile = getGrave(helper, CENTER);
    ServerPlayer player = mockPlayer(helper);
    tile.initTombstoneOwner(player);

    ItemStacksResourceHandler handler = tile.getHandler(null);
    try (var tx = Transaction.openRoot()) {
      ResourceHandlerUtil.insertStacking(handler, ItemResource.of(new ItemStack(Items.DIAMOND)), 10, tx);
      ResourceHandlerUtil.insertStacking(handler, ItemResource.of(new ItemStack(Items.IRON_HELMET)), 1, tx);
      tx.commit();
    }

    BlockPos absPos = helper.absolutePos(CENTER);
    BlockState state = helper.getLevel().getBlockState(absPos);
    BlockTomb.activatePlayerGrave(helper.getLevel(), absPos, state, player);

    helper.runAfterDelay(1, () -> {
      boolean hasDiamonds = false;
      boolean hasHelmet = false;
      for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
        ItemStack stack = player.getInventory().getItem(i);
        if (stack.is(Items.DIAMOND)) hasDiamonds = true;
        if (stack.is(Items.IRON_HELMET)) hasHelmet = true;
      }
      helper.assertTrue(hasDiamonds, "Player should have diamonds after activation");
      helper.assertTrue(hasHelmet, "Player should have helmet after activation");
      helper.assertTrue(helper.getLevel().getBlockState(absPos).isAir(), "Grave should be removed after activation");
      helper.succeed();
    });
  }

  @SuppressWarnings("removal")
  static void graveActivationDenied(GameTestHelper helper) {
    placeGraveWithFloor(helper, CENTER);
    BlockEntityTomb tile = getGrave(helper, CENTER);
    ServerPlayer owner = mockPlayer(helper);
    ServerPlayer intruder = mockPlayer(helper);
    tile.initTombstoneOwner(owner);

    ItemStacksResourceHandler handler = tile.getHandler(null);
    try (var tx = Transaction.openRoot()) {
      ResourceHandlerUtil.insertStacking(handler, ItemResource.of(new ItemStack(Items.DIAMOND)), 10, tx);
      tx.commit();
    }

    BlockPos absPos = helper.absolutePos(CENTER);
    BlockState state = helper.getLevel().getBlockState(absPos);
    BlockTomb.activatePlayerGrave(helper.getLevel(), absPos, state, intruder);

    helper.runAfterDelay(1, () -> {
      helper.assertBlockPresent(TombRegistry.GRAVE_SIMPLE.get(), CENTER);
      boolean intruderHasDiamonds = false;
      for (int i = 0; i < intruder.getInventory().getContainerSize(); i++) {
        if (intruder.getInventory().getItem(i).is(Items.DIAMOND)) {
          intruderHasDiamonds = true;
          break;
        }
      }
      helper.assertFalse(intruderHasDiamonds, "Non-owner should not receive items");
      helper.succeed();
    });
  }

  // === Auto-Equip ===

  @SuppressWarnings("removal")
  static void autoEquipArmor(GameTestHelper helper) {
    ServerPlayer player = mockPlayer(helper);

    boolean helmetEquipped = EntityHelper.autoEquip(new ItemStack(Items.IRON_HELMET), player);
    boolean chestEquipped = EntityHelper.autoEquip(new ItemStack(Items.IRON_CHESTPLATE), player);
    boolean legsEquipped = EntityHelper.autoEquip(new ItemStack(Items.IRON_LEGGINGS), player);
    boolean bootsEquipped = EntityHelper.autoEquip(new ItemStack(Items.IRON_BOOTS), player);

    helper.assertTrue(helmetEquipped, "Helmet should equip");
    helper.assertTrue(chestEquipped, "Chestplate should equip");
    helper.assertTrue(legsEquipped, "Leggings should equip");
    helper.assertTrue(bootsEquipped, "Boots should equip");
    helper.succeed();
  }

  @SuppressWarnings("removal")
  static void autoEquipShield(GameTestHelper helper) {
    ServerPlayer player = mockPlayer(helper);
    helper.assertTrue(player.getOffhandItem().isEmpty(), "Offhand should start empty");
    helper.assertTrue(EntityHelper.autoEquip(new ItemStack(Items.SHIELD), player), "Shield should equip");
    helper.assertTrue(player.getOffhandItem().is(Items.SHIELD), "Shield should be in offhand");
    helper.succeed();
  }

  @SuppressWarnings("removal")
  static void autoEquipElytra(GameTestHelper helper) {
    ServerPlayer player = mockPlayer(helper);
    helper.assertTrue(EntityHelper.autoEquip(new ItemStack(Items.ELYTRA), player), "Elytra should equip");
    helper.assertTrue(player.getInventory().getItem(36 + EquipmentSlot.CHEST.getIndex()).is(Items.ELYTRA), "Elytra should be in chest slot");
    helper.succeed();
  }

  @SuppressWarnings("removal")
  static void autoEquipElytraDisplace(GameTestHelper helper) {
    ServerPlayer player = mockPlayer(helper);
    int chestSlot = 36 + EquipmentSlot.CHEST.getIndex();
    player.getInventory().setItem(chestSlot, new ItemStack(Items.IRON_CHESTPLATE));

    helper.assertTrue(EntityHelper.autoEquip(new ItemStack(Items.ELYTRA), player), "Elytra should equip");
    helper.assertTrue(player.getInventory().getItem(chestSlot).is(Items.ELYTRA), "Elytra should now be in chest slot");

    boolean chestplateFound = false;
    for (int i = 0; i < 36; i++) {
      if (player.getInventory().getItem(i).is(Items.IRON_CHESTPLATE)) {
        chestplateFound = true;
        break;
      }
    }
    helper.assertTrue(chestplateFound, "Displaced chestplate should be in main inventory");
    helper.succeed();
  }

  @SuppressWarnings("removal")
  static void autoEquipBindingCurse(GameTestHelper helper) {
    ServerPlayer player = mockPlayer(helper);
    ItemStack cursedHelmet = new ItemStack(Items.IRON_HELMET);
    cursedHelmet.enchant(helper.getLevel().holderOrThrow(Enchantments.BINDING_CURSE), 1);

    helper.assertFalse(EntityHelper.autoEquip(cursedHelmet, player), "Binding curse item should not equip");
    helper.assertTrue(player.getInventory().getItem(36 + EquipmentSlot.HEAD.getIndex()).isEmpty(), "Head slot should remain empty");
    helper.succeed();
  }

  // === Grave Key ===

  static void graveKeyPosition(GameTestHelper helper) {
    ItemStack key = new ItemStack(TombRegistry.GRAVE_KEY.get());
    GlobalPos pos = new GlobalPos(helper.getLevel().dimension(), new BlockPos(100, 64, -200));

    helper.assertTrue(TombRegistry.GRAVE_KEY.get().setTombPos(key, pos), "setTombPos should return true");
    GlobalPos retrieved = TombRegistry.GRAVE_KEY.get().getTombPos(key);
    helper.assertTrue(retrieved.equals(pos), "Retrieved position should match stored position");
    helper.succeed();
  }

  @SuppressWarnings("removal")
  static void graveKeyRemoved(GameTestHelper helper) {
    placeGraveWithFloor(helper, CENTER);
    BlockEntityTomb tile = getGrave(helper, CENTER);
    ServerPlayer player = mockPlayer(helper);
    tile.initTombstoneOwner(player);

    ItemStacksResourceHandler handler = tile.getHandler(null);
    try (var tx = Transaction.openRoot()) {
      ResourceHandlerUtil.insertStacking(handler, ItemResource.of(new ItemStack(Items.DIAMOND)), 1, tx);
      tx.commit();
    }

    BlockPos absPos = helper.absolutePos(CENTER);
    GlobalPos graveGlobalPos = new GlobalPos(helper.getLevel().dimension(), absPos);
    ItemStack key = new ItemStack(TombRegistry.GRAVE_KEY.get());
    TombRegistry.GRAVE_KEY.get().setTombPos(key, graveGlobalPos);
    player.getInventory().add(key);

    helper.assertTrue(TombRegistry.GRAVE_KEY.get().countKeyInInventory(player) == 1, "Should have 1 key before activation");

    BlockState state = helper.getLevel().getBlockState(absPos);
    BlockTomb.activatePlayerGrave(helper.getLevel(), absPos, state, player);

    helper.runAfterDelay(1, () -> {
      helper.assertTrue(TombRegistry.GRAVE_KEY.get().countKeyInInventory(player) == 0, "Key should be removed after activation");
      helper.succeed();
    });
  }

  @SuppressWarnings("removal")
  static void graveKeyCount(GameTestHelper helper) {
    ServerPlayer player = mockPlayer(helper);
    helper.assertTrue(TombRegistry.GRAVE_KEY.get().countKeyInInventory(player) == 0, "Should start with 0 keys");

    player.getInventory().add(new ItemStack(TombRegistry.GRAVE_KEY.get()));
    player.getInventory().add(new ItemStack(TombRegistry.GRAVE_KEY.get()));
    player.getInventory().add(new ItemStack(TombRegistry.GRAVE_KEY.get()));

    helper.assertTrue(TombRegistry.GRAVE_KEY.get().countKeyInInventory(player) == 3, "Should have 3 keys");
    helper.succeed();
  }

  // === Full Inventory ===

  @SuppressWarnings("removal")
  static void fullInventoryDrops(GameTestHelper helper) {
    placeGraveWithFloor(helper, CENTER);
    BlockEntityTomb tile = getGrave(helper, CENTER);
    ServerPlayer player = mockPlayer(helper);
    tile.initTombstoneOwner(player);

    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
      player.getInventory().setItem(i, new ItemStack(Items.COBBLESTONE, 64));
    }

    ItemStacksResourceHandler handler = tile.getHandler(null);
    try (var tx = Transaction.openRoot()) {
      ResourceHandlerUtil.insertStacking(handler, ItemResource.of(new ItemStack(Items.EMERALD)), 5, tx);
      tx.commit();
    }

    BlockPos absPos = helper.absolutePos(CENTER);
    player.teleportTo(absPos.getX(), absPos.getY(), absPos.getZ());
    java.util.ArrayList<ItemEntity> capturedDrops = new java.util.ArrayList<>();
    player.captureDrops(capturedDrops);
    BlockState state = helper.getLevel().getBlockState(absPos);
    BlockTomb.activatePlayerGrave(helper.getLevel(), absPos, state, player);
    player.captureDrops(null);

    boolean emeraldsDropped = capturedDrops.stream().anyMatch(e -> e.getItem().is(Items.EMERALD));
    if (!emeraldsDropped) {
      AABB area = new AABB(absPos).inflate(5);
      List<ItemEntity> worldDrops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area);
      emeraldsDropped = worldDrops.stream().anyMatch(e -> e.getItem().is(Items.EMERALD));
    }
    helper.assertTrue(emeraldsDropped, "Emeralds should be dropped as entities when inventory is full");
    helper.succeed();
  }

  // === World Helper ===

  static void validPlacement(GameTestHelper helper) {
    BlockPos airPos = helper.absolutePos(new BlockPos(1, 5, 1));
    BlockPos waterPos = helper.absolutePos(new BlockPos(3, 1, 1));
    BlockPos stonePos = helper.absolutePos(new BlockPos(5, 1, 1));

    helper.setBlock(new BlockPos(3, 1, 1), Blocks.WATER);
    helper.setBlock(new BlockPos(5, 1, 1), Blocks.STONE);

    helper.assertTrue(WorldHelper.isValidPlacement(helper.getLevel(), airPos), "Air should be valid");
    helper.assertTrue(WorldHelper.isValidPlacement(helper.getLevel(), waterPos), "Water should be valid");
    helper.assertFalse(WorldHelper.isValidPlacement(helper.getLevel(), stonePos), "Stone should be invalid");
    helper.succeed();
  }
}
