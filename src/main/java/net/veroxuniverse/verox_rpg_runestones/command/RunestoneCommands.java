package net.veroxuniverse.verox_rpg_runestones.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.data.RunestoneEntry;
import net.veroxuniverse.verox_rpg_runestones.data.RunestoneRegistry;
import net.veroxuniverse.verox_rpg_runestones.teleport.RunestoneService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = RPGRunestones.MOD_ID)
public final class RunestoneCommands {

    private static final String KEY = "command." + RPGRunestones.MOD_ID + ".";

    private static final SuggestionProvider<CommandSourceStack> RUNESTONE_IDS = (context, builder) ->
            SharedSuggestionProvider.suggest(RunestoneRegistry.get(context.getSource().getServer()).all().stream()
                    .map(entry -> entry.id().toString()), builder);

    private RunestoneCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("runestones")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("list").executes(RunestoneCommands::list))
                .then(Commands.literal("teleport")
                        .then(Commands.argument("id", UuidArgument.uuid()).suggests(RUNESTONE_IDS)
                                .executes(RunestoneCommands::teleport)))
                .then(Commands.literal("remove")
                        .then(Commands.argument("id", UuidArgument.uuid()).suggests(RUNESTONE_IDS)
                                .executes(RunestoneCommands::remove)))
                .then(Commands.literal("setglobal")
                        .then(Commands.argument("id", UuidArgument.uuid()).suggests(RUNESTONE_IDS)
                                .then(Commands.argument("global", BoolArgumentType.bool())
                                        .executes(RunestoneCommands::setGlobal))))
                .then(Commands.literal("rename")
                        .then(Commands.argument("id", UuidArgument.uuid()).suggests(RUNESTONE_IDS)
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(RunestoneCommands::rename)))));
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        List<RunestoneEntry> entries = new ArrayList<>(RunestoneRegistry.get(context.getSource().getServer()).all());
        entries.sort(Comparator.comparing(RunestoneEntry::name, String.CASE_INSENSITIVE_ORDER));

        context.getSource().sendSuccess(() -> Component.translatable(KEY + "list.header", entries.size()).withStyle(ChatFormatting.AQUA), false);
        for (RunestoneEntry entry : entries) {
            MutableComponent line = Component.literal(" " + entry.name()).withStyle(entry.global() ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.WHITE)
                    .append(Component.literal("  " + entry.pos().toShortString() + "  " + entry.dimension().location()).withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("  "))
                    .append(Component.translatable(KEY + "list.teleport").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/runestones teleport " + entry.id()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(entry.id().toString())))));
            context.getSource().sendSuccess(() -> line, false);
        }
        return entries.size();
    }

    private static int teleport(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Optional<RunestoneEntry> entry = find(context);
        if (entry.isEmpty()) return 0;

        ServerLevel level = context.getSource().getServer().getLevel(entry.get().dimension());
        if (level == null) return 0;
        RunestoneService.teleportDirect(player, level, entry.get().pos());
        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> context) {
        Optional<RunestoneEntry> entry = find(context);
        if (entry.isEmpty()) return 0;

        RunestoneRegistry registry = RunestoneRegistry.get(context.getSource().getServer());
        ServerLevel level = context.getSource().getServer().getLevel(entry.get().dimension());
        registry.remove(entry.get().id());
        if (level != null) {
            level.removeBlock(entry.get().pos(), false);
        }
        context.getSource().sendSuccess(() -> Component.translatable(KEY + "removed", entry.get().name()), true);
        return 1;
    }

    private static int setGlobal(CommandContext<CommandSourceStack> context) {
        Optional<RunestoneEntry> entry = find(context);
        if (entry.isEmpty()) return 0;

        boolean global = BoolArgumentType.getBool(context, "global");
        RunestoneRegistry registry = RunestoneRegistry.get(context.getSource().getServer());
        registry.setGlobal(entry.get().id(), global);
        registry.get(entry.get().id()).ifPresent(updated -> RunestoneService.refreshBlockEntity(context.getSource().getServer(), updated));
        context.getSource().sendSuccess(() -> Component.translatable(KEY + (global ? "global_on" : "global_off"), entry.get().name()), true);
        return 1;
    }

    private static int rename(CommandContext<CommandSourceStack> context) {
        Optional<RunestoneEntry> entry = find(context);
        if (entry.isEmpty()) return 0;

        String name = StringArgumentType.getString(context, "name").trim();
        if (name.isEmpty() || name.length() > RunestoneService.MAX_NAME_LENGTH) {
            context.getSource().sendFailure(Component.translatable(KEY + "invalid_name", RunestoneService.MAX_NAME_LENGTH));
            return 0;
        }

        RunestoneRegistry registry = RunestoneRegistry.get(context.getSource().getServer());
        registry.rename(entry.get().id(), name);
        registry.get(entry.get().id()).ifPresent(updated -> RunestoneService.refreshBlockEntity(context.getSource().getServer(), updated));
        context.getSource().sendSuccess(() -> Component.translatable(KEY + "renamed", entry.get().name(), name), true);
        return 1;
    }

    private static Optional<RunestoneEntry> find(CommandContext<CommandSourceStack> context) {
        UUID id = UuidArgument.getUuid(context, "id");
        Optional<RunestoneEntry> entry = RunestoneRegistry.get(context.getSource().getServer()).get(id);
        if (entry.isEmpty()) {
            context.getSource().sendFailure(Component.translatable(KEY + "not_found"));
        }
        return entry;
    }
}