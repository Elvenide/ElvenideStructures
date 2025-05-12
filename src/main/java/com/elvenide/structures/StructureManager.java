package com.elvenide.structures;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface StructureManager<T extends Structure> {
    @Nullable T getNearby(Location loc);

    static @Nullable Structure getNearbyStructure(Location loc) {
        Structure elevator = ElvenideStructures.elevators().getNearby(loc);
        if (elevator != null)
            return elevator;

        return null;
    }

    static void useSwitch(Player user, Location loc) {
        Structure structure = getNearbyStructure(loc);
        if (structure != null)
            structure.onNearbySwitchUsed(user, loc);
    }
}
