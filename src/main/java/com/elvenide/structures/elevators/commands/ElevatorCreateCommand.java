package com.elvenide.structures.elevators.commands;

import com.elvenide.core.providers.command.SubArgumentBuilder;
import com.elvenide.core.providers.command.SubCommand;
import com.elvenide.core.providers.command.SubCommandBuilder;
import com.elvenide.core.providers.command.SubCommandContext;
import com.elvenide.core.providers.event.CoreEventHandler;
import com.elvenide.core.providers.event.CoreListener;
import com.elvenide.structures.ElvenideStructures;
import com.elvenide.structures.elevators.Elevator;
import com.elvenide.structures.selection.events.SelectionCancelEvent;
import com.elvenide.structures.selection.events.SelectionEvent;
import com.elvenide.structures.selection.tools.ElevatorSelectionTool;
import org.jetbrains.annotations.NotNull;

public class ElevatorCreateCommand implements SubCommand {
    static CoreListener creationListener = null;
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
            .addDouble("speed", 0.5, 10.0)
            .addDouble("walk_in_delay_secs", 0, 10.0, SubArgumentBuilder::setOptional);
    }

    @Override
    public void executes(@NotNull SubCommandContext context) {
        String name = context.args.getString("name");
        double speed = context.args.getDouble("speed");
        double moveDelay = context.args.getDouble("walk_in_delay_secs", 5);

        context.args
            .ifTrue(ElvenideStructures.elevators().getNames(context.player().getWorld()).contains(name))
            .thenEnd("An elevator with that name already exists!")
            .orIfTrue(creationListener != null)
            .thenEnd("Only one elevator can be edited at a time! Wait for {} to finish.", creatorName);

        ElevatorSelectionTool tool = new ElevatorSelectionTool(context.player());
        context.player().give(tool.getItem());
        context.reply("<green>Use the <smooth_purple>Selection Tool</smooth_purple> to select the elevator's carriage, which will move up and down.");
        context.reply("<yellow>Left-click <gray>to select corner #1.");
        context.reply("<yellow>Right-click <gray>to select opposite corner #2.");
        context.reply("<hover:show_text:'<gray>Bottom of elevator will move to align to this block.'><yellow>Shift-right-click <gray>to select destination floor.</hover>");
        context.reply("<yellow>Press F <gray>to finalize selection.");

        creatorName = context.player().getName();
        creationName = name;
        creationListener = new CoreListener() {
            @CoreEventHandler
            public void onSelection(SelectionEvent event) {
                context.player().getInventory().removeItem(event.selector().getInventory().getItemInMainHand());
                Elevator e = ElvenideStructures.elevators().create(name, speed, moveDelay, context.player().getWorld());

                e.setCarriageBlocks(event.selection());
                context.reply("<green>Created elevator '{}' with {}-block carriage!", e.getName(), e.getCarriageSize());

                creationListener.unregister();
                creatorName = "";
                creationListener = null;
            }

            @CoreEventHandler
            public void onCancel(SelectionCancelEvent event) {
                if (event.player().getName().equals(creatorName)) {
                    creationListener.unregister();
                    creationListener = null;
                    creatorName = "";
                }
            }
        };
        creationListener.register();
    }
}
