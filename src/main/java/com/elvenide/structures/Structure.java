package com.elvenide.structures;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface Structure {

    void onNearbySwitchUsed(Player user, Location loc);

    boolean isNearby(Location loc);

}
