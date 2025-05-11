package com.elvenide.structures.elevators.commands;

import com.elvenide.core.providers.command.SubCommand;
import com.elvenide.core.providers.command.SubCommandBuilder;
import com.elvenide.core.providers.command.SubCommandContext;
import com.elvenide.structures.ElvenideStructures;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.elvenide.structures.elevators.commands.ElevatorCreateCommand.*;

public class ElevatorDeleteCommand implements SubCommand {
    @Override
    public @NotNull String label() {
        return "delete";
    }

    @Override
    public void setup(@NotNull SubCommandBuilder builder) {
        builder
            .setDescription("<dark_red>Delete an elevator")
            .setPlayerOnly()
            .setPermission("elvenidestructures.commands.elevator.delete")
            .addWord("name", sub -> sub.suggests(ctx -> ElvenideStructures.elevators().getNames(ctx.player().getWorld())));
    }

    @Override
    public void executes(@NotNull SubCommandContext context) {
        String name = context.args.getString("name");

        context.args
            .ifTrue(!ElvenideStructures.elevators().getNames(context.player().getWorld()).contains(name))
            .thenEnd("An elevator with name '{}' does not exist!", name)
            .orIfTrue(creationListener != null && creationName.equals(name))
            .thenEnd("That elevator is currently being edited! Wait for {} to finish.", creatorName)
            .orIfTrue(Objects.requireNonNull(ElvenideStructures.elevators().get(name, context.player().getWorld())).isMoving())
            .thenEnd("Elevator '{}' is currently moving! Wait for it to finish.", name);

        ElvenideStructures.elevators().delete(name, context.player().getWorld());
        context.reply("<green>Deleted elevator '{}'.", name);
    }
}
