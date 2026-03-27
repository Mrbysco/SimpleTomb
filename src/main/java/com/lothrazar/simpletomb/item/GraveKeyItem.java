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
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Inventory;

import java.util.function.Consumer;

public class GraveKeyItem extends Item {

  public GraveKeyItem(Item.Properties properties) {
    super(properties.stacksTo(1).rarity(Rarity.UNCOMMON).sword(ToolMaterial.STONE, 3, -2.4F));
  }

  @Override
  public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
    if (entity instanceof Player player) {
      GlobalPos location = this.getTombPos(stack);
      if (location == null || location.equals(DeathHelper.ORIGIN) || !location.dimension().equals(level.dimension())) {
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
      }
      if (canTp) {
        if (count <= 1) {
          player.teleportTo(tombPos.getX(), tombPos.getY(), tombPos.getZ());
        }
        else if (level.isClientSide()) {
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
          if (!context.getLevel().isClientSide()) {
            BlockTomb.activatePlayerGrave(context.getLevel(), pos, state, player);
          }
          return InteractionResult.SUCCESS;
        }
      }
    }
    return InteractionResult.PASS;
  }

  @Override
  public InteractionResult use(Level level, Player playerIn, InteractionHand handIn) {
    playerIn.startUsingItem(handIn);
    return InteractionResult.SUCCESS;
  }

  @Override
  public ItemUseAnimation getUseAnimation(ItemStack stack) {
    return ItemUseAnimation.BOW;
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
    Level level = context.level();
    if (level != null && level.isClientSide() && com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT)) {
      GlobalPos location = this.getTombPos(stack);
      BlockPos pos = ClientUtils.getPlayerPos();
      if (pos != null && !location.equals(DeathHelper.ORIGIN)) {
        BlockPos tombPos = location.pos();
        int distance = (int) getDistance(tombPos, pos);
        tooltipAdder.accept(Component.translatable(MessageType.MESSAGE_DISTANCE.getKey(),
            distance, tombPos.getX(), tombPos.getY(), tombPos.getZ(), WorldHelper.getDimensionName(location.dimension()))
            .withStyle(ChatFormatting.DARK_PURPLE));
      }
    }
    super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);
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

  public boolean removeKeyForGraveInInventory(Player player, GlobalPos graveLoc) {
    Inventory inv = player.getInventory();
    for (int i = 0; i < inv.getContainerSize(); ++i) {
      ItemStack stack = inv.getItem(i);
      if (stack.getItem() == TombRegistry.GRAVE_KEY.get() &&
          TombRegistry.GRAVE_KEY.get().getTombPos(stack).equals(graveLoc)) {
        inv.removeItem(i, 1);
        return true;
      }
    }
    return false;
  }

  public int countKeyInInventory(Player player) {
    int count = 0;
    Inventory inv = player.getInventory();
    for (int i = 0; i < inv.getContainerSize(); i++) {
      if (inv.getItem(i).getItem() == TombRegistry.GRAVE_KEY.get()) {
        count++;
      }
    }
    return count;
  }
}
