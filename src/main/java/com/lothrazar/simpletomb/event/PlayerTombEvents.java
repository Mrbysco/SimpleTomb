package com.lothrazar.simpletomb.event;

import com.lothrazar.simpletomb.ConfigTomb;
import com.lothrazar.simpletomb.ModTomb;
import com.lothrazar.simpletomb.TombRegistry;
import com.lothrazar.simpletomb.block.BlockEntityTomb;
import com.lothrazar.simpletomb.block.BlockTomb;
import com.lothrazar.simpletomb.data.DeathHelper;
import com.lothrazar.simpletomb.data.MessageType;
import com.lothrazar.simpletomb.data.PartEnum;
import com.lothrazar.simpletomb.data.PlayerTombRecords;
import com.lothrazar.simpletomb.helper.EntityHelper;
import com.lothrazar.simpletomb.helper.WorldHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent.Detonate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerTombEvents {

  public final Map<UUID, PlayerTombRecords> grv = new HashMap<>();
  private static final String TOMB_FILE_EXT = ".mctomb";
  private static final String TB_SOULBOUND_STACKS = "tb_soulbound_stacks";

  public PlayerTombRecords findGrave(UUID id) {
    if (grv.containsKey(id)) {
      return grv.get(id);
    }
    return null;
  }

  @SubscribeEvent(priority = EventPriority.HIGH)
  public void onPlayerLogged(PlayerEvent.PlayerLoggedInEvent event) {
    if (EntityHelper.isValidPlayerMP(event.getEntity())) {
      ServerPlayer player = (ServerPlayer) event.getEntity();
      CompoundTag playerData = player.getPersistentData();
      CompoundTag persistantData;
      if (playerData.contains(EntityHelper.NBT_PLAYER_PERSISTED)) {
        persistantData = playerData.getCompoundOrEmpty(EntityHelper.NBT_PLAYER_PERSISTED);
      }
      else {
        persistantData = new CompoundTag();
        playerData.put(EntityHelper.NBT_PLAYER_PERSISTED, persistantData);
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onDetonate(Detonate event) {
    event.getAffectedBlocks().removeIf(blockPos -> (event.getLevel().getBlockState(blockPos).getBlock() instanceof BlockTomb));
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onDestroy(LivingDestroyBlockEvent event) {
    if (event.getState().getBlock() instanceof BlockTomb && !(event.getEntity() instanceof Player)) {
      event.setCanceled(true);
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    Player player = event.getEntity();
    if (EntityHelper.isValidPlayerMP(player) && !player.isSpectator()) {
      CompoundTag persistentTag = EntityHelper.getPersistentTag(player);
      ListTag stackList = persistentTag.getListOrEmpty(TB_SOULBOUND_STACKS);
      for (int i = 0; i < stackList.size(); ++i) {
        ItemStack stack = ItemStack.CODEC.parse(
            player.registryAccess().createSerializationContext(NbtOps.INSTANCE),
            stackList.getCompoundOrEmpty(i)
        ).result().orElse(ItemStack.EMPTY);
        if (!stack.isEmpty()) {
          ItemHandlerHelper.giveItemToPlayer(player, stack);
        }
      }
      persistentTag.remove(TB_SOULBOUND_STACKS);
      player.inventoryMenu.broadcastChanges();
    }
  }

  private void storeSoulboundsOnBody(Player player, List<ItemStack> keys) {
    CompoundTag persistentTag = EntityHelper.getPersistentTag(player);
    ListTag stackList = persistentTag.contains(TB_SOULBOUND_STACKS) ? persistentTag.getListOrEmpty(TB_SOULBOUND_STACKS) : new ListTag();
    for (ItemStack key : keys) {
      Tag encoded = ItemStack.CODEC.encodeStart(
          player.registryAccess().createSerializationContext(NbtOps.INSTANCE),
          key
      ).result().orElse(new CompoundTag());
      stackList.add(encoded);
    }
    persistentTag.put(TB_SOULBOUND_STACKS, stackList);
    keys.clear();
  }

  @SubscribeEvent(receiveCanceled = true)
  public void onLivingDeath(LivingDeathEvent event) {
    if (!EntityHelper.isValidPlayer(event.getEntity()) ||
        WorldHelper.isRuleKeepInventory((Player) event.getEntity())) {
      return;
    }
    if (!event.isCanceled()) {
      Player player = (Player) event.getEntity();
      Inventory inventory = player.getInventory();
      PartEnum part = ConfigTomb.KEEPPARTS.get();
      switch (part) {
        case HOTBAR -> {
          List<ItemStack> hotbarStacks = new ArrayList<>();
          for (int i = 0; i < 9; i++) {
            hotbarStacks.add(i, inventory.getItem(i));
          }
          keepingMap.put(player.getUUID(), hotbarStacks);
        }
        case ARMOR -> {
          List<ItemStack> armorStacks = new ArrayList<>();
          for (int i = 0; i < 4; i++) {
            armorStacks.add(inventory.getItem(36 + i));
          }
          keepingMap.put(player.getUUID(), armorStacks);
        }
        case HOTBAR_AND_ARMOR -> {
          List<ItemStack> stacks = new ArrayList<>();
          for (int i = 0; i < 9; i++) {
            stacks.add(i, inventory.getItem(i));
          }
          for (int i = 0; i < 4; i++) {
            stacks.add(inventory.getItem(36 + i));
          }
          keepingMap.put(player.getUUID(), stacks);
        }
        case NONE -> {}
      }
    }
  }

  private final static Map<UUID, List<ItemStack>> keepingMap = new HashMap<>();

  @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
  public void onPlayerDrops(LivingDropsEvent event) {
    if (!ConfigTomb.TOMBENABLED.get()) {
      return;
    }
    if (!EntityHelper.isValidPlayer(event.getEntity()) ||
        WorldHelper.isRuleKeepInventory((Player) event.getEntity())) {
      return;
    }
    Player player = (Player) event.getEntity();
    List<ItemStack> keepStacks = keepingMap.getOrDefault(player.getUUID(), new ArrayList<>());
    if (!keepStacks.isEmpty()) {
      event.getDrops().removeIf(entity -> keepStacks.contains(entity.getItem()));
      this.storeSoulboundsOnBody(player, keepStacks);
      keepingMap.remove(player.getUUID());
    }
    saveBackup(event);
    placeTombstone(event);
  }

  @SubscribeEvent
  public void onSaveFile(PlayerEvent.SaveToFile event) {
    Player player = event.getEntity();
    File mctomb = new File(event.getPlayerDirectory(), player.getUUID() + TOMB_FILE_EXT);
    if (grv.containsKey(player.getUUID())) {
      PlayerTombRecords dataToSave = grv.get(player.getUUID());
      CompoundTag data = dataToSave.write();
      try {
        FileOutputStream fileoutputstream = new FileOutputStream(mctomb);
        NbtIo.writeCompressed(data, fileoutputstream);
        fileoutputstream.close();
      }
      catch (IOException e) {
        ModTomb.LOGGER.error("IO", e);
      }
    }
  }

  @SubscribeEvent
  public void onLoadFile(PlayerEvent.LoadFromFile event) {
    Player player = event.getEntity();
    File mctomb = new File(event.getPlayerDirectory(), player.getUUID() + TOMB_FILE_EXT);
    if (mctomb.exists()) {
      try {
        FileInputStream fileinputstream = new FileInputStream(mctomb);
        CompoundTag data = NbtIo.readCompressed(fileinputstream, NbtAccounter.unlimitedHeap());
        fileinputstream.close();
        PlayerTombRecords dataLoaded = new PlayerTombRecords();
        dataLoaded.read(data, player.getUUID());
        grv.put(player.getUUID(), dataLoaded);
      }
      catch (Exception e) {
        ModTomb.LOGGER.error("IO", e);
      }
    }
  }

  private void saveBackup(LivingDropsEvent event) {
    ServerPlayer player = (ServerPlayer) event.getEntity();
    Iterator<ItemEntity> it = event.getDrops().iterator();
    ListTag drops = new ListTag();
    boolean isEmpty = true;
    CompoundTag tombstoneTag = new CompoundTag();
    while (it.hasNext()) {
      ItemEntity entityItem = it.next();
      if (entityItem != null && !entityItem.getItem().isEmpty()) {
        ItemStack stack = entityItem.getItem();
        Tag encoded = ItemStack.CODEC.encodeStart(
            event.getEntity().registryAccess().createSerializationContext(NbtOps.INSTANCE),
            stack
        ).result().orElse(new CompoundTag());
        drops.add(encoded);
        if (stack.getItem() != TombRegistry.GRAVE_KEY.get()) {
          isEmpty = false;
        }
      }
    }
    if (!isEmpty) {
      tombstoneTag.putLong("timestamp", System.currentTimeMillis());
      tombstoneTag.put("drops", drops);
      Tag posTag = BlockPos.CODEC.encodeStart(
          player.registryAccess().createSerializationContext(NbtOps.INSTANCE),
          player.blockPosition()
      ).result().orElse(new CompoundTag());
      tombstoneTag.put("pos", posTag);
      tombstoneTag.putString("dimension", player.level().dimension().identifier().toString());
      UUID pid = player.getUUID();
      tombstoneTag.putString("playerid", pid.toString());
      tombstoneTag.putString("playername", player.getDisplayName().getString());
      if (grv.containsKey(pid)) {
        grv.get(pid).playerGraves.add(tombstoneTag);
      }
      else {
        grv.put(pid, new PlayerTombRecords(pid, tombstoneTag));
      }
    }
  }

  private void placeTombstone(LivingDropsEvent event) {
    ServerPlayer player = (ServerPlayer) event.getEntity();
    ServerLevel level = (ServerLevel) player.level();
    Iterator<ItemEntity> it = event.getDrops().iterator();
    ArrayList<ItemStack> keys = new ArrayList<>();
    while (it.hasNext()) {
      ItemEntity entityItem = it.next();
      if (entityItem != null && !entityItem.getItem().isEmpty()) {
        ItemStack stack = entityItem.getItem();
        if (stack.getItem() == TombRegistry.GRAVE_KEY.get()) {
          keys.add(stack.copy());
          it.remove();
        }
      }
    }
    List<ItemEntity> itemsPickedUpFromGround = pickupFromGround(player, keys);
    this.storeSoulboundsOnBody(player, keys);
    boolean hasDrop = event.getDrops().size() > 0 || itemsPickedUpFromGround.size() > 0;
    if (!hasDrop) {
      MessageType.MESSAGE_NO_LOOT_FOR_GRAVE.sendSpecialMessage(player);
      return;
    }
    BlockPos initPos = WorldHelper.getInitialPos(level, new BlockPos(player.blockPosition()));
    GlobalPos spawnPos = WorldHelper.findGraveSpawn(player, initPos);
    if (spawnPos == null || spawnPos.pos() == null) {
      MessageType.MESSAGE_NO_PLACE_FOR_GRAVE.sendSpecialMessage(player);
      ModTomb.LOGGER.log(Level.INFO, MessageType.MESSAGE_NO_PLACE_FOR_GRAVE.getTranslation());
      return;
    }
    Direction facing = player.getDirection().getOpposite();
    BlockState state = getRandomGrave(level, facing);
    boolean wasPlaced = WorldHelper.placeGrave(level, spawnPos.pos(), state);
    if (!wasPlaced) {
      sendFailMessage(player);
      return;
    }
    BlockEntity tile = level.getBlockEntity(spawnPos.pos());
    if (!(tile instanceof BlockEntityTomb grave)) {
      sendFailMessage(player);
      return;
    }
    grave.initTombstoneOwner(player);
    IItemHandler itemHandler = grave.getHandler(null);
    if (ConfigTomb.KEYGIVEN.get()) {
      ItemStack key = new ItemStack(TombRegistry.GRAVE_KEY.get());
      TombRegistry.GRAVE_KEY.get().setTombPos(key, spawnPos);
      setKeyName(player, key);
      keys.add(key);
    }
    this.storeSoulboundsOnBody(player, keys);
    for (ItemEntity entityItem : event.getDrops()) {
      if (!entityItem.getItem().isEmpty()) {
        ItemHandlerHelper.insertItemStacked(itemHandler, entityItem.getItem().copy(), false);
        entityItem.setItem(ItemStack.EMPTY);
      }
    }
    for (ItemEntity entityItem : itemsPickedUpFromGround) {
      ItemHandlerHelper.insertItemStacked(itemHandler, entityItem.getItem(), false);
      entityItem.setItem(ItemStack.EMPTY);
    }
    level.sendBlockUpdated(spawnPos.pos(), state, state, 2);
    DeathHelper.INSTANCE.putLastGrave(player, spawnPos);
    if (ConfigTomb.TOMBLOG.get()) {
	    ModTomb.LOGGER.info("{}{}", MessageType.MESSAGE_NEW_GRAVE.getTranslation(), String.format("(%d, %d, %d) %s", spawnPos.pos().getX(), spawnPos.pos().getY(), spawnPos.pos().getZ(), WorldHelper.getDimensionName(spawnPos.dimension()).getString()));
    }
    if (ConfigTomb.TOMBCHAT.get()) {
      MessageType.MESSAGE_NEW_GRAVE.sendSpecialMessage(player, String.format("(%d, %d, %d) " + WorldHelper.getDimensionName(spawnPos.dimension()).getString(),
              spawnPos.pos().getX(), spawnPos.pos().getY(), spawnPos.pos().getZ()));
    }
  }

  public static void sendFailMessage(ServerPlayer player) {
    MessageType.MESSAGE_FAIL_TO_PLACE_GRAVE.sendSpecialMessage(player);
    ModTomb.LOGGER.log(Level.INFO, MessageType.MESSAGE_FAIL_TO_PLACE_GRAVE.getTranslation());
  }

  public static void setKeyName(ServerPlayer player, ItemStack key) {
    putKeyName(player.getName().getString(), key);
  }

  public static void putKeyName(String player, ItemStack key) {
    if (ConfigTomb.KEYNAMED.get()) {
      MutableComponent text = Component.translatable(player);
      text.append(Component.literal(" "));
      text.append(key.getHoverName());
      text.withStyle(ChatFormatting.GOLD);
      key.set(DataComponents.CUSTOM_NAME, text);
    }
  }

  static BlockState getRandomGrave(ServerLevel serverLevel, Direction facing) {
    BlockTomb[] graves = new BlockTomb[] {
        TombRegistry.GRAVE_SIMPLE.get(),
        TombRegistry.GRAVE_NORMAL.get(),
        TombRegistry.GRAVE_CROSS.get(),
        TombRegistry.TOMBSTONE.get(),
    };
    BlockState state = graves[serverLevel.random.nextInt(graves.length)].defaultBlockState();
    state = state.setValue(BlockTomb.FACING, facing);
    state = state.setValue(BlockTomb.MODEL_TEXTURE, serverLevel.random.nextInt(2));
    return state;
  }

  private List<ItemEntity> pickupFromGround(Player player, ArrayList<ItemStack> keys) {
    double range = ConfigTomb.TOMBEXTRAITEMS.get();
    if (range == 0) {
      return new ArrayList<>();
    }
    int posX = player.blockPosition().getX();
    int posY = player.blockPosition().getY();
    int posZ = player.blockPosition().getZ();
    return player.level().getEntitiesOfClass(ItemEntity.class, new AABB(
        posX - range,
        posY - range,
        posZ - range,
        posX + range,
        posY + range,
        posZ + range));
  }
}
