package com.elvenide.structures;

import com.elvenide.core.ElvenideCore;
import com.elvenide.core.plugin.CorePlugin;
import com.elvenide.structures.elevators.ElevatorManager;
import com.elvenide.structures.elevators.commands.ElevatorCreateCommand;
import com.elvenide.structures.elevators.commands.ElevatorDeleteCommand;
import com.elvenide.structures.elevators.commands.ElevatorRunCommand;
import com.elvenide.structures.selection.SelectionManager;

public final class ElvenideStructures extends CorePlugin {

    private static final ElevatorManager elevatorManager = new ElevatorManager();

    @Override
    public void onLoaded() {
        ElvenideCore.text.packages.moreColors.install();
    }

    @Override
    public void onEnabled() {
        registerListeners(elevatorManager);
        registerListeners(new SelectionManager());

        ElvenideCore.commands.setHeader("<gradient:aqua:dark_aqua>Elvenide Structures");

        ElvenideCore.commands.create("elvenidestructures")
            .setAliases("estructures", "estruct")
            .setDescription("<dark_aqua>Create moving structures!")
            .addSubGroup("elevator", builder -> {
                builder
                    .addSubCommand(new ElevatorCreateCommand())
                    .addSubCommand(new ElevatorDeleteCommand())
                    .addSubCommand(new ElevatorRunCommand());
            })
            .addHelpSubCommand();

        ElvenideCore.commands.register(this);
    }

    public static ElevatorManager elevators() {
        return elevatorManager;
    }

}
