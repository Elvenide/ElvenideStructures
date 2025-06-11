package com.elvenide.structures;

import com.elvenide.core.Core;
import com.elvenide.core.providers.config.Config;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface StructureManager<T extends Structure> {
    @Nullable T getNearby(Location loc);

    default Config getStructures(World world) {
        return Core.config.get("../../" + world.getName() + "/elvenide_structures/structures.dat");
    }

    default Config getConfiguration(World world) {
        return Core.config.get("../../" + world.getName() + "/elvenide_structures/config.yml", "config.yml");
    }

    static @Nullable Structure getNearbyStructure(Location loc) {
        Structure elevator = ElvenideStructures.elevators().getNearby(loc);
        if (elevator != null)
            return elevator;

        return ElvenideStructures.doors().getNearby(loc);
    }

    static void useSwitch(Player user, Location loc) {
        Structure structure = getNearbyStructure(loc);
        if (structure != null)
            structure.onNearbySwitchUsed(user, loc);
    }
}
