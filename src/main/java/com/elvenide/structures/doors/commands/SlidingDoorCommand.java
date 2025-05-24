package com.elvenide.structures.doors.commands;

import com.elvenide.core.Core;
import com.elvenide.core.providers.command.SubCommand;
import com.elvenide.core.providers.command.SubCommandBuilder;
import com.elvenide.core.providers.command.SubCommandContext;
import com.elvenide.structures.ElvenideStructures;
import com.elvenide.structures.doors.Door;
import com.elvenide.structures.doors.types.SlidingDoor;
import com.elvenide.structures.selection.events.SelectionEvent;
import com.elvenide.structures.selection.tools.SlidingDoorSelectionTool;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import static com.elvenide.structures.doors.DoorManager.creationListener;
import static com.elvenide.structures.doors.DoorManager.creatorName;
import static com.elvenide.structures.doors.DoorManager.creationName;

public class SlidingDoorCommand implements SubCommand {

    @Override
    public @NotNull String label() {
        return "sliding_door";
    }

    @Override
    public void setup(@NotNull SubCommandBuilder builder) {
        builder
            .setDescription("<dark_red>Create a sliding door.")
            .setPlayerOnly()
            .setPermission("elvenidestructures.commands.door")
            .addWord("name")
            .addFloat("slide_duration_secs", 0.05f, 100f);
    }

    @Override
    public void executes(@NotNull SubCommandContext context) {
        String name = context.args.getString("name");
        float slideDuration = context.args.getFloat("slide_duration_secs");

        context.args
            .ifTrue(ElvenideStructures.doors().getNames(context.player().getWorld()).contains(name))
            .thenEnd("A door with that name already exists!")
            .orIfTrue(creationListener != null)
            .thenEnd("Only one door can be edited at a time! Wait for {} to finish.", creatorName);

        SlidingDoorSelectionTool tool = new SlidingDoorSelectionTool();
        context.player().give(tool.getItem());
        context.reply("<green>Use the <smooth_purple>Selection Tool</smooth_purple> to select the door.");
        context.reply("<yellow>Left-click <gray>to select corner #1.");
        context.reply("<yellow>Right-click <gray>to select opposite corner #2.");
        context.reply("<yellow>Shift-click <gray>to make the door slide in the direction you are facing.");
        context.reply("<yellow>Press F <gray>to finalize selection.");

        creatorName = context.player().getName();
        creationName = name;
        creationListener = new Listener() {
            @EventHandler
            public void onSelection(SelectionEvent event) {
                context.player().getInventory().removeItem(event.selector().getInventory().getItemInMainHand());
                Door door = SlidingDoor.create(name, slideDuration, event.selection(), tool);

                context.reply("<green>Created door '{}' with {} blocks!", name, door.getBlocks().size());
                ElvenideStructures.unregisterListeners(creationListener);
                creatorName = "";
                creationListener = null;
            }

            @EventHandler
            public void onLeave(PlayerQuitEvent event) {
                if (event.getPlayer().getName().equals(creatorName)) {
                    creationListener = null;
                    creatorName = "";
                    ElvenideStructures.unregisterListeners(creationListener);
                }
            }

            @EventHandler
            public void onWorldChange(PlayerChangedWorldEvent event) {
                if (event.getPlayer().getName().equals(creatorName)) {
                    creationListener = null;
                    creatorName = "";
                    ElvenideStructures.unregisterListeners(creationListener);
                    Core.text.send(event.getPlayer(), "<red>Door creation process cancelled due to world change.");
                }
            }
        };
        ElvenideStructures.registerListeners(creationListener);
    }
}
