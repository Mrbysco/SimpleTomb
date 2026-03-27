package com.lothrazar.simpletomb.block;

import com.lothrazar.simpletomb.TombRegistry;
import com.lothrazar.simpletomb.data.MessageType;
import com.lothrazar.simpletomb.helper.EntityHelper;
import com.lothrazar.simpletomb.helper.WorldHelper;
import com.lothrazar.simpletomb.proxy.ClientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.players.NameAndId;
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
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.stream.IntStream;

public class BlockEntityTomb extends BlockEntity {

  private static final int SOULTIMER = 100;

  public BlockEntityTomb(BlockPos pos, BlockState blockState) {
    super(TombRegistry.TOMBSTONE_BLOCK_ENTITY.get(), pos, blockState);
  }

  private final ItemStacksResourceHandler handler = new ItemStacksResourceHandler(120);
  protected String ownerName = "";
  protected long deathDate;
  public int timer = 0;
  protected UUID ownerId = null;
  private boolean onlyOwnersAccess = true;

  public void giveInventory(@Nullable Player player) {
    if (!this.level.isClientSide() && player != null && !(player instanceof FakePlayer)) {
      try (var tx = Transaction.openRoot()) {
        for (int i = handler.size() - 1; i >= 0; --i) {
          ItemResource resource = handler.getResource(i);
          int amount = handler.getAmountAsInt(i);
          ItemStack stack = resource.toStack(amount);
          if (EntityHelper.autoEquip(stack, player)) {
            handler.extract(resource, amount, tx);
          }
        }

        IntStream.range(0, handler.size()).forEach(ix -> {
          ItemResource resource = handler.getResource(ix);
          int amount = handler.getAmountAsInt(ix);
          ItemStack stack = resource.toStack(amount);
          if (!stack.isEmpty()) {
            player.getInventory().add(stack.copy());
            handler.extract(resource, amount, tx);
          }
        });

        tx.commit();
      }
      this.removeGraveBy(player);
      if (player.inventoryMenu != null) {
        player.inventoryMenu.broadcastChanges();
      }
      MessageType.MESSAGE_OPEN_GRAVE_SUCCESS.sendSpecialMessage(player);
    }
  }

  public void dropInventory(Level level, BlockPos pos) {
    if (this.level != null && !this.level.isClientSide()) {
      try (var tx = Transaction.openRoot()) {
        for (int i = 0; i < handler.size(); ++i) {
          ItemResource resource = handler.getResource(i);
          int amount = handler.getAmountAsInt(i);
          ItemStack stack = resource.toStack(amount);
          if (!stack.isEmpty()) {
            Containers.dropItemStack(
                    level,
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    stack);
            handler.extract(resource, amount, tx);
          }
        }
        tx.commit();
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
      handler.serialize(output);
    }
    output.putBoolean("onlyOwnersAccess", this.onlyOwnersAccess);
  }

  @Override
  public void loadAdditional(ValueInput input) {
    this.ownerName = input.getStringOr("ownerName", "");
    this.deathDate = input.getLongOr("deathDate", 0L);
    this.timer = input.getIntOr("countTicks", 0);
    if (handler != null) {
      handler.deserialize(input);
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

  public ItemStacksResourceHandler getHandler(@Nullable Direction direction) {
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
