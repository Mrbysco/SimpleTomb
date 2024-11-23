package com.lothrazar.simpletomb.event;

import com.lothrazar.simpletomb.ModTomb;
import com.lothrazar.simpletomb.TombRegistry;
import com.lothrazar.simpletomb.block.BlockEntityTomb;
import com.lothrazar.simpletomb.data.PlayerTombRecords;
import com.lothrazar.simpletomb.data.TombCommands;
import com.lothrazar.simpletomb.helper.WorldHelper;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandEvents {

  private static final String ARG_SELECTED = "selected";
  private static final String ARG_PLAYER = "player";

  @SubscribeEvent
  public void onRegisterCommandsEvent(RegisterCommandsEvent event) {
    CommandDispatcher<CommandSourceStack> r = event.getDispatcher();
    r.register(LiteralArgumentBuilder.<CommandSourceStack> literal(ModTomb.MODID)
        .requires((p) -> p.hasPermission(3))
        .then(Commands.literal(TombCommands.RESTORE.toString())
            .then(Commands.argument(ARG_PLAYER, GameProfileArgument.gameProfile()).suggests((cs, b) -> buildPlayerArg(cs, b))
                .then(Commands.argument(ARG_SELECTED, IntegerArgumentType.integer())
                    .executes(x -> {
                      return exeRestore(x, getPlayerProfile(x), IntegerArgumentType.getInteger(x, ARG_SELECTED));
                    }))))
        .then(Commands.literal(TombCommands.KEY.toString())
            .then(Commands.argument(ARG_PLAYER, GameProfileArgument.gameProfile()).suggests((cs, b) -> buildPlayerArg(cs, b))
                .then(Commands.argument(ARG_SELECTED, IntegerArgumentType.integer())
                    .executes(x -> {
                      return exeKey(x, getPlayerProfile(x), IntegerArgumentType.getInteger(x, ARG_SELECTED));
                    }))))
        .then(Commands.literal(TombCommands.LIST.toString())
            .then(Commands.argument(ARG_PLAYER, GameProfileArgument.gameProfile()).suggests((cs, b) -> buildPlayerArg(cs, b))
                .executes(x -> {
                  return exeList(x, getPlayerProfile(x));
                })))
        .then(Commands.literal(TombCommands.DELETE.toString())
            .then(Commands.argument(ARG_PLAYER, GameProfileArgument.gameProfile()).suggests((cs, b) -> buildPlayerArg(cs, b))
                .executes(x -> {
                  return exeDelete(x, getPlayerProfile(x));
                })))
    // more go here
    );
  }

  private CompletableFuture<Suggestions> buildPlayerArg(CommandContext<CommandSourceStack> cs, SuggestionsBuilder b) {
    return SharedSuggestionProvider.suggest(cs.getSource().getServer().getPlayerList().getPlayers().stream().map(p -> p.getGameProfile().getName()), b);
  }

  private GameProfile getPlayerProfile(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    return GameProfileArgument.getGameProfiles(ctx, ARG_PLAYER).stream().findFirst().orElse(null);
  }

  private int exeDelete(CommandContext<CommandSourceStack> ctx, GameProfile target) throws CommandSyntaxException {
    PlayerTombRecords found = ModTomb.GLOBAL.findGrave(target.getId());
    if (found != null) {
      int previous = found.playerGraves.size();
      found.deleteAll();
      MutableComponent msg = Component.translatable("Deleted: " + previous);
      msg.setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD));
      ctx.getSource().sendSuccess(() -> msg, false);
    }
    return 0;
  }

  private int exeList(CommandContext<CommandSourceStack> ctx, GameProfile target) throws CommandSyntaxException {
    PlayerTombRecords found = ModTomb.GLOBAL.findGrave(target.getId());
    if (found != null && found.playerGraves.size() > 0) {
      for (int i = 0; i < found.playerGraves.size(); i++) {
        MutableComponent msg = Component.translatable(found.toDisplayString(i, ctx.getSource().registryAccess()));
        msg.setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD));
        ctx.getSource().sendSuccess(() -> msg, false);
      }
    }
    else {
      MutableComponent msg = Component.literal("Found: #0");
      msg.setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD));
      ctx.getSource().sendSuccess(() -> msg, false);
    }
    return 0;
  }

  private int exeKey(CommandContext<CommandSourceStack> ctx, GameProfile target, int index) throws CommandSyntaxException {
    PlayerTombRecords found = ModTomb.GLOBAL.findGrave(target.getId());
    if (found != null) {
      CompoundTag grave = found.playerGraves.get(index);
      if (grave == null) {
	      ModTomb.LOGGER.error("Invalid grave index {}; try between 0 and  {}", index, found.playerGraves.size() - 1);
        return 1;
      }
      GlobalPos spawnPos = new GlobalPos(PlayerTombRecords.getDim(grave), PlayerTombRecords.getPos(grave));
      ItemStack key = new ItemStack(TombRegistry.GRAVE_KEY.get());
      TombRegistry.GRAVE_KEY.get().setTombPos(key, spawnPos);
      PlayerTombEvents.putKeyName(target.getName(), key);
      // key for u
      MutableComponent msg = Component.translatable("Attempting to give the key for tomb [" + index + "] to player " + target.getName() + ":" + target.getId());
      ctx.getSource().sendSuccess(() -> msg, false);
      ServerPlayer user = ctx.getSource().getServer().getPlayerList().getPlayer(target.getId());
      ItemHandlerHelper.giveItemToPlayer(user, key);
    }
    return 0;
  }

  private int exeRestore(CommandContext<CommandSourceStack> ctx, GameProfile target, int index) throws CommandSyntaxException {
    ctx.getSource().sendSuccess(() -> Component.translatable("Attempting to restore tomb [" + index + "] for player " + target.getName() + ":" + target.getId()), false);
    PlayerTombRecords found = ModTomb.GLOBAL.findGrave(target.getId());
    if (found != null) {
      CompoundTag grave = found.playerGraves.get(index);
      if (grave == null) {
	      ModTomb.LOGGER.error("Invalid grave index {}; try between 0 and  {}", index, found.playerGraves.size() - 1);
        return 1;
      }
      BlockPos pos = PlayerTombRecords.getPos(grave);
      ResourceKey<Level> dim = PlayerTombRecords.getDim(grave);
      //      ModTomb.LOGGER.error("found  at" + pos + " in " + dim);
      List<ItemStack> drops = PlayerTombRecords.getDrops(grave, ctx.getSource().registryAccess());
      //      ModTomb.LOGGER.error("items contained " + drops.size());
      //TODO: is this dupe code from location class?
      ServerLevel targetWorld = ctx.getSource().getServer().getLevel(dim);
      BlockState state = PlayerTombEvents.getRandomGrave(targetWorld, Direction.NORTH);
      boolean wasPlaced = WorldHelper.placeGrave(targetWorld, pos, state);
      if (wasPlaced) {
        //fill it up
        BlockEntityTomb tile = (BlockEntityTomb) targetWorld.getBlockEntity(pos);
        tile.initTombstoneOwner(target);
        IItemHandler itemHandler = targetWorld.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        //        ItemHandlerHelper.ins
        for (ItemStack d : drops) {
          ItemHandlerHelper.insertItemStacked(itemHandler, d.copy(), false);
        }
      }
      ctx.getSource().sendSuccess(() -> Component.literal("Restored tomb with at [")
              .append(Component.literal(pos.toShortString()).withStyle(ChatFormatting.YELLOW)).append("] in ")
              .append(Component.translatable(dim.location().toLanguageKey("dimension")).withStyle(ChatFormatting.GOLD))
              .withStyle(ChatFormatting.GREEN), false);
    }
    return 0;
  }
  //
  //  private void badCommandMsg(ServerPlayerEntity player) {
  //    //.setStyle(Style.EMPTY.setFormatting(TextFormatting.GOLD))
  //    player.sendMessage(new TranslationTextComponent(ModTomb.MODID + ".commands.null"), player.getUniqueID());
  //    player.sendMessage(new TranslationTextComponent("[" + String.join(", ", SUBCOMMANDS) + "]"), player.getUniqueID());
  //  }
}
