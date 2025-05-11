package com.elvenide.structures.elevators.commands;

import com.elvenide.core.Core;
import com.elvenide.core.providers.command.SubCommand;
import com.elvenide.core.providers.command.SubCommandBuilder;
import com.elvenide.core.providers.command.SubCommandContext;
import com.elvenide.structures.ElvenideStructures;
import com.elvenide.structures.elevators.Elevator;
import com.elvenide.structures.selection.events.SelectionEvent;
import com.elvenide.structures.selection.SelectionTool;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class ElevatorCreateCommand implements SubCommand {
    static Listener creationListener = null;
    static String creatorName = "";
    static String creationName = "";

    @Override
    public @NotNull String label() {
        return "create";
    }

    @Override
    public void setup(@NotNull SubCommandBuilder builder) {
        builder
            .setDescription("<dark_red>Create a new elevator")
            .setPlayerOnly()
            .setPermission("elvenidestructures.commands.elevator.create")
            .addWord("name")
            .addDouble("speed", 0.5, 10.0);
    }

    @Override
    public void executes(@NotNull SubCommandContext context) {
        String name = context.args.getString("name");
        double speed = context.args.getDouble("speed");

        context.args
            .ifTrue(ElvenideStructures.elevators().getNames(context.player().getWorld()).contains(name))
            .thenEnd("An elevator with that name already exists!")
            .orIfTrue(creationListener != null)
            .thenEnd("Only one elevator can be edited at a time! Wait for {} to finish.", creatorName);

        context.player().give(new SelectionTool().create());
        context.reply("<green>Use the <smooth_purple>Selection Tool</smooth_purple> to select the elevator's carriage, which will move up and down.");
        context.reply("<yellow>Left-click <gray>to select corner #1.");
        context.reply("<yellow>Right-click <gray>to select opposite corner #2.");
        context.reply("<yellow>Shift-click <gray>to finalize selection.");

        creatorName = context.player().getName();
        creationName = name;
        creationListener = new Listener() {
            @EventHandler
            public void onSelection(SelectionEvent event) {
                context.player().getInventory().removeItem(event.selector().getInventory().getItemInMainHand());
                Elevator e = ElvenideStructures.elevators().create(name, speed, context.player().getWorld());

                try {
                    e.setCarriageBlocks(event.selection());
                    context.reply("<green>Created elevator '{}' with {}-block carriage!", e.getName(), e.getCarriageSize());
                } catch (IllegalStateException ex) {
                    context.reply("<red>Failed to create elevator '{}': There is no space for players to stand in your elevator!", e.getName());
                }

                HandlerList.unregisterAll(this);
                creatorName = "";
                creationListener = null;
            }

            @EventHandler
            public void onLeave(PlayerQuitEvent event) {
                if (event.getPlayer().getName().equals(creatorName)) {
                    creationListener = null;
                    creatorName = "";
                    HandlerList.unregisterAll(this);
                }
            }

            @EventHandler
            public void onWorldChange(PlayerChangedWorldEvent event) {
                if (event.getPlayer().getName().equals(creatorName)) {
                    creationListener = null;
                    creatorName = "";
                    HandlerList.unregisterAll(this);
                    Core.text.send(event.getPlayer(), "<red>Elevator creation process cancelled due to world change.");
                }
            }
        };
        ElvenideStructures.registerListeners(creationListener);
    }
}
