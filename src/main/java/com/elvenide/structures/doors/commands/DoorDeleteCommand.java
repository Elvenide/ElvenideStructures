package com.elvenide.structures.doors.commands;

import com.elvenide.core.providers.command.SubCommand;
import com.elvenide.core.providers.command.SubCommandBuilder;
import com.elvenide.core.providers.command.SubCommandContext;
import com.elvenide.structures.ElvenideStructures;
import org.jetbrains.annotations.NotNull;

import static com.elvenide.structures.doors.DoorManager.creationListener;
import static com.elvenide.structures.doors.DoorManager.creatorName;
import static com.elvenide.structures.doors.DoorManager.creationName;

public class DoorDeleteCommand implements SubCommand {
    @Override
    public @NotNull String label() {
        return "delete";
    }

    @Override
    public void setup(@NotNull SubCommandBuilder builder) {
        builder
            .setDescription("<dark_red>Delete a door")
            .setPlayerOnly()
            .setPermission("elvenidestructures.commands.door.delete")
            .addWord("name", sub -> sub.suggests(ctx -> ElvenideStructures.doors().getNames(ctx.player().getWorld())));
    }

    @Override
    public void executes(@NotNull SubCommandContext context) {
        String name = context.args.getString("name");

        context.args
            .ifTrue(!ElvenideStructures.doors().getNames(context.player().getWorld()).contains(name))
            .thenEnd("A door with name '{}' does not exist!", name)
            .orIfTrue(creationListener != null && creationName.equals(name))
            .thenEnd("That door is currently being edited! Wait for {} to finish.", creatorName)
            .orIfTrue(ElvenideStructures.doors().isMoving(context.player().getWorld(), name))
            .thenEnd("Door '{}' is currently moving! Wait for it to finish.", name);

        ElvenideStructures.doors().delete(context.player().getWorld(), name);
        context.reply("<green>Deleted door '{}'.", name);
    }
}
