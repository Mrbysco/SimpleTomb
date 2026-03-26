package com.lothrazar.simpletomb.block;

import com.lothrazar.simpletomb.TombRegistry;
import com.lothrazar.simpletomb.data.MessageType;
import com.lothrazar.simpletomb.helper.EntityHelper;
import com.lothrazar.simpletomb.helper.WorldHelper;
import com.lothrazar.simpletomb.proxy.ClientUtils;
import net.minecraft.server.players.NameAndId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class BlockEntityTomb extends BlockEntity {

  private static final int SOULTIMER = 100;

  public BlockEntityTomb(BlockPos pos, BlockState blockState) {
    super(TombRegistry.TOMBSTONE_BLOCK_ENTITY.get(), pos, blockState);
  }

  private final ItemStackHandler handler = new ItemStackHandler(120);
  protected String ownerName = "";
  protected long deathDate;
  public int timer = 0;
  protected UUID ownerId = null;
  private boolean onlyOwnersAccess = true;

  public void giveInventory(@Nullable Player player) {
    if (!this.level.isClientSide() && player != null && !(player instanceof FakePlayer)) {
      for (int i = handler.getSlots() - 1; i >= 0; --i) {
        if (EntityHelper.autoEquip(handler.getStackInSlot(i), player)) {
          handler.extractItem(i, 64, false);
        }
      }
      IntStream.range(0, handler.getSlots()).forEach(ix -> {
        ItemStack stack = handler.getStackInSlot(ix);
        if (!stack.isEmpty()) {
          ItemHandlerHelper.giveItemToPlayer(player, stack.copy());
          handler.extractItem(ix, 64, false);
        }
      });
      this.removeGraveBy(player);
      if (player.inventoryMenu != null) {
        player.inventoryMenu.broadcastChanges();
      }
      MessageType.MESSAGE_OPEN_GRAVE_SUCCESS.sendSpecialMessage(player);
    }
  }

  public void dropInventory(Level level, BlockPos pos) {
    if (this.level != null && !this.level.isClientSide()) {
      for (int i = 0; i < handler.getSlots(); ++i) {
        ItemStack stack = handler.getStackInSlot(i);
        if (!stack.isEmpty()) {
          Containers.dropItemStack(
              level,
              pos.getX(),
              pos.getY(),
              pos.getZ(),
                  handler.extractItem(i, stack.getCount(), false));
        }
      }
    }
  }

  public boolean onlyOwnersCanAccess() {
    return this.onlyOwnersAccess;
  }

  private void removeGraveBy(@Nullable Player player) {
    if (this.level != null) {
      WorldHelper.removeNoEvent(this.level, this.worldPosition);
      if (player != null) {
        this.level.playSound(player,
            player.blockPosition(),
            SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
      }
    }
  }

  public void initTombstoneOwner(Player owner) {
    this.deathDate = System.currentTimeMillis();
    this.ownerName = owner.getDisplayName().getString();
    this.ownerId = owner.getUUID();
  }

  public void initTombstoneOwner(NameAndId owner) {
    this.deathDate = 0;
    this.ownerName = owner.name();
    this.ownerId = owner.id();
  }

  public boolean isOwner(Player owner) {
    if (ownerId == null || owner == null || !hasOwner()) {
      return false;
    }
    return this.ownerId.equals(owner.getUUID());
  }

  String getOwnerName() {
    return this.ownerName;
  }

  boolean hasOwner() {
    return ownerName != null && ownerName.length() > 0;
  }

  long getOwnerDeathTime() {
    return this.deathDate;
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    output.putString("ownerName", this.ownerName);
    output.putLong("deathDate", this.deathDate);
    output.putInt("countTicks", this.timer);
    if (this.ownerId != null) {
      output.putString("ownerid", this.ownerId.toString());
    }
    if (handler != null) {
      List<ItemStack> stacks = new ArrayList<>();
      for (int i = 0; i < handler.getSlots(); i++) {
        stacks.add(handler.getStackInSlot(i));
      }
      output.store("inv", ItemStack.CODEC.listOf(), stacks);
    }
    output.putBoolean("onlyOwnersAccess", this.onlyOwnersAccess);
  }

  @Override
  public void loadAdditional(ValueInput input) {
    this.ownerName = input.getStringOr("ownerName", "");
    this.deathDate = input.getLongOr("deathDate", 0L);
    this.timer = input.getIntOr("countTicks", 0);
    if (handler != null) {
      List<ItemStack> stacks = input.read("inv", ItemStack.CODEC.listOf()).orElse(List.of());
      for (int i = 0; i < Math.min(stacks.size(), handler.getSlots()); i++) {
        handler.setStackInSlot(i, stacks.get(i));
      }
      for (int i = stacks.size(); i < handler.getSlots(); i++) {
        handler.setStackInSlot(i, ItemStack.EMPTY);
      }
    }
    String ownerIdStr = input.getStringOr("ownerid", "");
    if (!ownerIdStr.isEmpty()) {
      try {
        this.ownerId = UUID.fromString(ownerIdStr);
      } catch (IllegalArgumentException e) {
        this.ownerId = null;
      }
    }
    this.onlyOwnersAccess = input.getBooleanOr("onlyOwnersAccess", true);
    super.loadAdditional(input);
  }

  public ItemStackHandler getHandler(@Nullable Direction direction) {
    return handler;
  }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
    CompoundTag compound = new CompoundTag();
    compound.putString("ownerName", this.ownerName);
    compound.putLong("deathDate", this.deathDate);
    compound.putInt("countTicks", this.timer);
    return compound;
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public boolean triggerEvent(int id, int type) {
    return true;
  }

  public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, BlockEntityTomb tile) {
    ClientUtils.produceGraveSmoke(level, tile.worldPosition.getX(), tile.worldPosition.getY(), tile.worldPosition.getZ());
    tile.timer++;
    if (tile.timer % SOULTIMER == 0) {
      ClientUtils.produceGraveSoul(level, tile.worldPosition);
      tile.timer = 1;
    }
    if (level.isClientSide()) {
      ClientUtils.produceGraveSmoke(level, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }
  }

  public static <E extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, BlockEntityTomb tile) {
    tile.timer++;
    if ((tile.timer - 1) % SOULTIMER == 0) {
      tile.timer = 1;
    }
  }
}
