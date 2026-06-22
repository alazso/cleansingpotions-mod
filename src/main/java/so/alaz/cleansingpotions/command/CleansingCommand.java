package so.alaz.cleansingpotions.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import so.alaz.cleansingpotions.CleansingPotions;
import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.item.MilkItemFactory;
import so.alaz.cleansingpotions.item.MilkVariant;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public final class CleansingCommand {

    private static final SuggestionProvider<CommandSourceStack> POTIONS =
        (ctx, builder) -> SharedSuggestionProvider.suggest(List.of("cleansing", "soothing", "purging"), builder);
    private static final SuggestionProvider<CommandSourceStack> VARIANTS =
        (ctx, builder) -> SharedSuggestionProvider.suggest(List.of("regular", "splash", "lingering"), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("cleansingpotions")
            .requires(adminCheck())
            .then(Commands.literal("reload")
                .executes(CleansingCommand::reload))
            .then(Commands.literal("give")
                .then(Commands.argument("potion", StringArgumentType.word())
                    .suggests(POTIONS)
                    .executes(ctx -> give(ctx, MilkVariant.REGULAR, List.of(ctx.getSource().getPlayerOrException())))
                    .then(Commands.argument("variant", StringArgumentType.word())
                        .suggests(VARIANTS)
                        .executes(ctx -> give(ctx, variantArg(ctx), List.of(ctx.getSource().getPlayerOrException())))
                        .then(Commands.argument("targets", EntityArgument.players())
                            .executes(ctx -> give(ctx, variantArg(ctx), EntityArgument.getPlayers(ctx, "targets")))))));
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(root);
        dispatcher.register(Commands.literal("cleanse")
            .requires(adminCheck())
            .redirect(node));
    }

    private static Predicate<CommandSourceStack> adminCheck() {
        //? if <1.21.11 {
        return source -> source.hasPermission(2);
        //?} else {
        /*return Commands.<CommandSourceStack>hasPermission(Commands.LEVEL_GAMEMASTERS);*/
        //?}
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        CleansingPotions.reload();
        ctx.getSource().sendSuccess(() -> Component.translatable("command.cleansingpotions.reloaded"), true);
        return 1;
    }

    private static MilkVariant variantArg(CommandContext<CommandSourceStack> ctx) {
        return MilkVariant.fromTag(StringArgumentType.getString(ctx, "variant"));
    }

    private static int give(CommandContext<CommandSourceStack> ctx, MilkVariant variant,
                            Collection<ServerPlayer> targets) throws CommandSyntaxException {
        String potion = StringArgumentType.getString(ctx, "potion");
        CleanseMode mode = CleanseMode.fromPotionName(potion);
        if (mode == null) {
            ctx.getSource().sendFailure(
                Component.translatable("command.cleansingpotions.unknown_potion", potion));
            return 0;
        }
        if (variant == null) {
            ctx.getSource().sendFailure(
                Component.translatable("command.cleansingpotions.unknown_variant",
                    StringArgumentType.getString(ctx, "variant")));
            return 0;
        }
        for (ServerPlayer target : targets) {
            ItemStack item = MilkItemFactory.create(variant, mode);
            if (!target.getInventory().add(item)) {
                target.drop(item, false);
            }
        }
        Component label = Component.translatable("potion.cleansingpotions." + mode.potionName());
        if (targets.size() == 1) {
            ServerPlayer only = targets.iterator().next();
            ctx.getSource().sendSuccess(() -> Component.translatable(
                "command.cleansingpotions.given", label, only.getDisplayName()), true);
        } else {
            ctx.getSource().sendSuccess(() -> Component.translatable(
                "command.cleansingpotions.given_many", label, targets.size()), true);
        }
        return targets.size();
    }

    private CleansingCommand() {
    }
}
