package com.elvenide.structures.doors;

import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;

public record DoorBlock(Location initialLocation, BlockDisplay display) {
}
