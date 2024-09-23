package com.lothrazar.simpletomb.block;

import com.lothrazar.simpletomb.TombRegistry;
import com.lothrazar.simpletomb.data.MessageType;
import com.lothrazar.simpletomb.helper.EntityHelper;
import com.lothrazar.simpletomb.helper.WorldHelper;
import com.lothrazar.simpletomb.proxy.ClientUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

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
  //nothing in game sets this.  
  // a server command could set this to false to let admins or anyone in 
  //but in normal survival gameplay, it stays true and thus requires owners to access their graves
  private boolean onlyOwnersAccess = true;

  public void giveInventory(@Nullable Player player) {
    if (!this.level.isClientSide && player != null && !(player instanceof FakePlayer)) {
      //
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
    if (this.level != null && !this.level.isClientSide) {
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

  public void initTombstoneOwner(GameProfile owner) {
    this.deathDate = 0;
    this.ownerName = owner.getName();
    this.ownerId = owner.getId();
  }

  public boolean isOwner(Player owner) {
    if (ownerId == null || owner == null || !hasOwner()) {
      return false;
    }
    //dont match on name. id is always set anyway 
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
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.putString("ownerName", this.ownerName);
    tag.putLong("deathDate", this.deathDate);
    tag.putInt("countTicks", this.timer);
    if (this.ownerId != null) {
      tag.putUUID("ownerid", this.ownerId);
    }
    if (handler != null) {
      tag.put("inv", handler.serializeNBT(registries));
    }
    tag.putBoolean("onlyOwnersAccess", this.onlyOwnersAccess);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    this.ownerName = compound.getString("ownerName");
    this.deathDate = compound.getLong("deathDate");
    this.timer = compound.getInt("countTicks");
    CompoundTag invTag = compound.getCompound("inv");
    if (handler != null) {
        handler.deserializeNBT(registries, invTag);
    }
    if (compound.hasUUID("ownerid")) {
      this.ownerId = compound.getUUID("ownerid");
    }
    this.onlyOwnersAccess = compound.getBoolean("onlyOwnersAccess");
    super.loadAdditional(compound, registries);
  }

  public ItemStackHandler getHandler(@Nullable Direction direction) {
    return handler;
  }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
    CompoundTag compound = new CompoundTag();
    this.saveAdditional(compound, provider);
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

  @Override
  public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider provider) {
    loadAdditional(pkt.getTag(), provider);
  }

  public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, BlockEntityTomb tile) {
    ClientUtils.produceGraveSmoke(level, tile.worldPosition.getX(), tile.worldPosition.getY(), tile.worldPosition.getZ());
    tile.timer++;
    if (tile.timer % SOULTIMER == 0) {
      ClientUtils.produceGraveSoul(level, tile.worldPosition);
      tile.timer = 1;
    }
    if (level.isClientSide) {
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
