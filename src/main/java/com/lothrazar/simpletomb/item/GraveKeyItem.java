package com.lothrazar.simpletomb.item;

import com.lothrazar.simpletomb.ConfigTomb;
import com.lothrazar.simpletomb.TombRegistry;
import com.lothrazar.simpletomb.block.BlockTomb;
import com.lothrazar.simpletomb.data.DeathHelper;
import com.lothrazar.simpletomb.data.MessageType;
import com.lothrazar.simpletomb.helper.NBTHelper;
import com.lothrazar.simpletomb.helper.WorldHelper;
import com.lothrazar.simpletomb.proxy.ClientUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

public class GraveKeyItem extends SwordItem {

  public GraveKeyItem(Item.Properties properties) {
    super(Tiers.STONE, properties.stacksTo(1).attributes(SwordItem.createAttributes(Tiers.STONE, 3, -2.4F)));
  }

  @Override
  
  public Component getDescription() {
    return Component.translatable(this.getDescriptionId()).withStyle(ChatFormatting.GOLD);
  }

  @Override
  public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
    if (entity instanceof Player player) {
      GlobalPos location = this.getTombPos(stack);
      if (location == null || !location.equals(DeathHelper.ORIGIN) || !location.equals(level.dimension())) {
        return;
      }
      BlockPos tombPos = location.pos();
      double distance = getDistance(tombPos, player.blockPosition());
      boolean canTp = false;
      if (player.isCreative()) {
        canTp = ConfigTomb.TPCREATIVE.get();
      }
      else {
        canTp = (ConfigTomb.TPSURVIVAL.get() > 0 &&
            distance < ConfigTomb.TPSURVIVAL.get()) || ConfigTomb.TPSURVIVAL.get() == -1;
        //-1 is magic value for ANY DISTANCE IS OK
      }
      if (canTp) {
        if (count <= 1) {
          //teleport happens here
          player.teleportTo(tombPos.getX(), tombPos.getY(), tombPos.getZ());
        }
        else if (level.isClientSide) {
          //not done, and can TP
          ClientUtils.produceParticleCasting(entity, p -> !p.isUsingItem());
        }
      }
    }
  }

  private double getDistance(BlockPos pos, BlockPos pos2) {
    double deltX = pos.getX() - pos2.getX();
    double deltY = pos.getY() - pos2.getY();
    double deltZ = pos.getZ() - pos2.getZ();
    return Math.sqrt(deltX * deltX + deltY * deltY + deltZ * deltZ);
  }

  @Override
  public int getUseDuration(ItemStack stack, LivingEntity entity) {
    return 86;
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    if (ConfigTomb.KEYOPENONUSE.get()) {
      BlockPos pos = context.getClickedPos();
      Player player = context.getPlayer();
      if (player.getItemInHand(context.getHand()).getItem() == TombRegistry.GRAVE_KEY.get()) {
        BlockState state = context.getLevel().getBlockState(pos);
        if (state.getBlock() instanceof BlockTomb) {
          //open me
          BlockTomb.activatePlayerGrave(context.getLevel(), pos, state, player);
          return InteractionResult.SUCCESS;
        }
      }
    }
    return InteractionResult.PASS;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player playerIn, InteractionHand handIn) {
    ItemStack itemstack = playerIn.getItemInHand(handIn);
    playerIn.startUsingItem(handIn);
    return InteractionResultHolder.success(itemstack);
  }

  @Override
  public UseAnim getUseAnimation(ItemStack stack) {
    return UseAnim.BOW;
  }

  @Override
  
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag tooltipFlag) {
    if (Screen.hasShiftDown()) {
      GlobalPos location = this.getTombPos(stack);
      //      this.addItemPosition(list, this.getTombPos(stack));
      Player player = Minecraft.getInstance().player;
      if (player != null && !location.equals(DeathHelper.ORIGIN)) {
        BlockPos pos = player.blockPosition();
        BlockPos tombPos = location.pos();
        int distance = (int) getDistance(tombPos, pos);
        list.add(Component.translatable(MessageType.MESSAGE_DISTANCE.getKey(),
            distance, tombPos.getX(), tombPos.getY(), tombPos.getZ(), WorldHelper.getDimensionName(location.dimension()))
            .withStyle(ChatFormatting.DARK_PURPLE));
      }
    }
    super.appendHoverText(stack, context, list, tooltipFlag);
  }

  public boolean setTombPos(ItemStack stack, GlobalPos location) {
    if (stack.getItem() == this && !location.equals(DeathHelper.ORIGIN)) {
      NBTHelper.setLocation(stack, location);
      return true;
    }
    return false;
  }

  public GlobalPos getTombPos(ItemStack stack) {
    return stack.getItem() == this
        ? NBTHelper.getLocation(stack)
        : DeathHelper.ORIGIN;
  }

  /**
   * Look for any key that matches this Location and remove that key from player
   */
  public boolean removeKeyForGraveInInventory(Player player, GlobalPos graveLoc) {
    IItemHandler itemHandler = player.getCapability(Capabilities.ItemHandler.ENTITY, null);
    if (itemHandler != null) {
      for (int i = 0; i < itemHandler.getSlots(); ++i) {
        ItemStack stack = itemHandler.getStackInSlot(i);
        if (stack.getItem() == TombRegistry.GRAVE_KEY.get() &&
            TombRegistry.GRAVE_KEY.get().getTombPos(stack).equals(graveLoc)) {
          itemHandler.extractItem(i, 1, false);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * How many keys, ignoring data. casts long to int
   */
  public int countKeyInInventory(Player player) {
    return (int) player.getInventory().items.stream()
        .filter(stack -> stack.getItem() == TombRegistry.GRAVE_KEY.get())
        .count();
  }
}
