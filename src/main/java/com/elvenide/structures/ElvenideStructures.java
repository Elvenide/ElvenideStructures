package com.elvenide.structures;

import com.elvenide.core.Core;
import com.elvenide.core.providers.command.SubCommand;
import com.elvenide.core.providers.command.SubCommandBuilder;
import com.elvenide.core.providers.command.SubCommandContext;
import com.elvenide.core.providers.plugin.CorePlugin;
import com.elvenide.structures.doors.DoorManager;
import com.elvenide.structures.doors.commands.DoorDeleteCommand;
import com.elvenide.structures.doors.commands.SlidingDoorCommand;
import com.elvenide.structures.elevators.ElevatorManager;
import com.elvenide.structures.elevators.commands.ElevatorCreateCommand;
import com.elvenide.structures.elevators.commands.ElevatorDeleteCommand;
import com.elvenide.structures.elevators.commands.ElevatorRunCommand;
import com.elvenide.structures.switches.SwitchListener;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class ElvenideStructures extends CorePlugin {

    private static final ElevatorManager elevatorManager = new ElevatorManager();
    private static final DoorManager doorManager = new DoorManager();

    @Override
    public void onLoaded() {
        Core.text.packages.moreColors.install();
    }

    @Override
    public void onEnabled() {
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        registerListeners(elevatorManager); // Register Bukkit listener for elevators
        elevatorManager.register(); // Register Core listener for elevators
        registerListeners(doorManager);
        registerListeners(new SwitchListener());

        Core.commands.setHeader("<gradient:aqua:dark_aqua>Elvenide Structures");
        Core.commands.create("elvenidestructures")
            .setAliases("estructures", "estruct")
            .setDescription("<dark_aqua>Create moving structures!")
            .addSubGroup("elevator", builder -> {
                builder
                    .addSubCommand(new ElevatorCreateCommand())
                    .addSubCommand(new ElevatorDeleteCommand())
                    .addSubCommand(new ElevatorRunCommand());
            })
            .addSubGroup("door", builder -> {
                builder
                    .addSubGroup("create", create -> {
                        create
                            .addSubCommand(new SlidingDoorCommand());
                    })
                    .addSubCommand(new DoorDeleteCommand());
            })
            .addSubCommand(new SubCommand() {
                @Override
                public @NotNull String label() {
                    return "reload";
                }

                @Override
                public void setup(@NotNull SubCommandBuilder builder) {
                    builder
                        .setDescription("<dark_red>Reloads the plugin configurations in the current world")
                        .setPermission("elvenidestructures.commands.reload")
                        .setPlayerOnly();
                }

                @Override
                public void executes(@NotNull SubCommandContext context) {
                    World world = context.player().getWorld();
                    elevatorManager.getConfiguration(world).reload();
                    elevatorManager.getStructures(world).reload();
                    doorManager.getConfiguration(world).reload();
                    doorManager.getStructures(world).reload();
                    context.reply("<green>Configurations successfully reloaded in world '{}'!", world.getName());
                }
            })
            .addHelpSubCommand();
    }

    public static ElevatorManager elevators() {
        return elevatorManager;
    }

    public static DoorManager doors() {
        return doorManager;
    }

}
