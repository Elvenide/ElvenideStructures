package com.elvenide.structures.elevators.commands;

import com.elvenide.core.providers.command.SubCommand;
import com.elvenide.core.providers.command.SubCommandBuilder;
import com.elvenide.core.providers.command.SubCommandContext;
import com.elvenide.structures.ElvenideStructures;
import com.elvenide.structures.elevators.Elevator;
import org.jetbrains.annotations.NotNull;

import static com.elvenide.structures.elevators.commands.ElevatorCreateCommand.*;

public class ElevatorRunCommand implements SubCommand {
    @Override
    public @NotNull String label() {
        return "run";
    }

    @Override
    public void setup(@NotNull SubCommandBuilder builder) {
        builder
            .setDescription("<dark_red>Runs an elevator from its current floor to its destination floor.")
            .setPlayerOnly()
            .setPermission("elvenidestructures.commands.elevator.run")
            .addWord("name", sub -> sub.suggests(ctx -> ElvenideStructures.elevators().getNames(ctx.player().getWorld())));
    }

    @Override
    public void executes(@NotNull SubCommandContext context) {
        String name = context.args.getString("name");

        context.args
            .ifTrue(!ElvenideStructures.elevators().getNames(context.player().getWorld()).contains(name))
            .thenEnd("An elevator with that name does not exist!")
            .orIfTrue(creationListener != null && creationName.equals(name))
            .thenEnd("That elevator is currently being edited! Wait for {} to finish.", creatorName);

        Elevator e = ElvenideStructures.elevators().get(name, context.player().getWorld());
        try {
            assert e != null;
            e.moveWithoutCooldown();
        } catch (IllegalStateException ex) {
            context.end(ex.getMessage());
        }
    }
}
