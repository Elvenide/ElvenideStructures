package com.elvenide.structures;

import com.elvenide.core.api.PublicAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface Structure {

    void onNearbySwitchUsed(Player user, Location loc);

    @PublicAPI
    boolean isNearby(Location loc);

}
