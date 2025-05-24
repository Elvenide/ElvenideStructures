package com.elvenide.structures.selection;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Selection implements Iterable<Location> {
    private final World world;
    private final List<Location> positions = new ArrayList<>();

    public Selection(Location firstPos, Location secondPos) {
        this.world = firstPos.getWorld();
        firstPos = firstPos.toBlockLocation();
        secondPos = secondPos.toBlockLocation();

        for (int x = Math.min(firstPos.getBlockX(), secondPos.getBlockX()); x <= Math.max(firstPos.getBlockX(), secondPos.getBlockX()); x++) {
            for (int y = Math.min(firstPos.getBlockY(), secondPos.getBlockY()); y <= Math.max(firstPos.getBlockY(), secondPos.getBlockY()); y++) {
                for (int z = Math.min(firstPos.getBlockZ(), secondPos.getBlockZ()); z <= Math.max(firstPos.getBlockZ(), secondPos.getBlockZ()); z++) {
                    Location loc = new Location(firstPos.getWorld(), x, y, z);
                    if (!loc.getBlock().getType().isAir())
                        positions.add(loc);
                }
            }
        }
    }

    @Override
    public @NotNull Iterator<Location> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < positions.size();
            }

            @Override
            public Location next() {
                return positions.get(index++);
            }
        };
    }

    public World getWorld() {
        return world;
    }
}
