package com.lothrazar.simpletomb.item;

import java.util.List;
import javax.annotation.Nullable;
import com.lothrazar.simpletomb.ConfigTomb;
import com.lothrazar.simpletomb.TombRegistry;
import com.lothrazar.simpletomb.block.BlockTomb;
import com.lothrazar.simpletomb.data.LocationBlockPos;
import com.lothrazar.simpletomb.data.MessageType;
import com.lothrazar.simpletomb.helper.NBTHelper;
import com.lothrazar.simpletomb.helper.WorldHelper;
import com.lothrazar.simpletomb.proxy.ClientUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

public class GraveKeyItem extends SwordItem {

  private static final String TOMB_POS = "tombPos";

  public GraveKeyItem(Item.Properties properties) {
    super(Tiers.STONE, 3, -2.4F, properties.stacksTo(1));
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public Component getDescription() {
    return Component.translatable(this.getDescriptionId()).withStyle(ChatFormatting.GOLD);
  }

  @Override
  public void onUsingTick(ItemStack stack, LivingEntity entity, int timeLeft) {
    if (entity instanceof Player player) {
      LocationBlockPos location = this.getTombPos(stack);
      if (location == null || location.isOrigin()
          || !location.dim.equalsIgnoreCase(WorldHelper.dimensionToString(player.level))) {
        return;
      }
      double distance = location.getDistance(player.blockPosition());
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
        if (timeLeft <= 1) {
          //teleport happens here
          BlockPos pos = location.toBlockPos();
          player.teleportTo(pos.getX(), pos.getY(), pos.getZ());
        }
        else if (entity.level.isClientSide) {
          //not done, and can TP
          ClientUtils.produceParticleCasting(entity, p -> !p.isUsingItem());
        }
      }
    }
  }

  @Override
  public int getUseDuration(ItemStack stack) {
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
          BlockTomb.activatePlayerGrave(player.level, pos, state, player);
          return InteractionResult.SUCCESS;
        }
      }
    }
    return InteractionResult.PASS;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
    ItemStack itemstack = playerIn.getItemInHand(handIn);
    playerIn.startUsingItem(handIn);
    return InteractionResultHolder.success(itemstack);
  }

  @Override
  public UseAnim getUseAnimation(ItemStack stack) {
    return UseAnim.BOW;
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
    if (Screen.hasShiftDown()) {
      LocationBlockPos location = this.getTombPos(stack);
      //      this.addItemPosition(list, this.getTombPos(stack));
      Player player = Minecraft.getInstance().player;
      if (player != null && !location.isOrigin()) {
        BlockPos pos = player.blockPosition();
        int distance = (int) location.getDistance(pos);
        list.add(Component.translatable(MessageType.MESSAGE_DISTANCE.getKey(),
            distance, location.x, location.y, location.z, location.dim)
            .withStyle(ChatFormatting.DARK_PURPLE));
      }
    }
    super.appendHoverText(stack, world, list, flag);
  }

  public boolean setTombPos(ItemStack stack, LocationBlockPos location) {
    if (stack.getItem() == this && !location.isOrigin()) {
      NBTHelper.setLocation(stack, TOMB_POS, location);
      return true;
    }
    return false;
  }

  public LocationBlockPos getTombPos(ItemStack stack) {
    return stack.getItem() == this
        ? NBTHelper.getLocation(stack, TOMB_POS)
        : LocationBlockPos.ORIGIN;
  }

  /**
   * Look for any key that matches this Location and remove that key from player
   */
  public boolean removeKeyForGraveInInventory(Player player, LocationBlockPos graveLoc) {
    IItemHandler itemHandler = player.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
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
