package com.elvenide.structures;

import com.elvenide.core.Core;
import com.elvenide.core.providers.plugin.CorePlugin;
import com.elvenide.structures.elevators.ElevatorManager;
import com.elvenide.structures.elevators.commands.ElevatorCreateCommand;
import com.elvenide.structures.elevators.commands.ElevatorDeleteCommand;
import com.elvenide.structures.elevators.commands.ElevatorRunCommand;
import com.elvenide.structures.selection.SelectionManager;
import com.elvenide.structures.switches.SwitchListener;

public final class ElvenideStructures extends CorePlugin {

    private static final ElevatorManager elevatorManager = new ElevatorManager();

    @Override
    public void onLoaded() {
        Core.text.packages.moreColors.install();
    }

    @Override
    public void onEnabled() {
        registerListeners(elevatorManager);
        registerListeners(new SelectionManager());
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
            .addHelpSubCommand();
    }

    public static ElevatorManager elevators() {
        return elevatorManager;
    }

}
