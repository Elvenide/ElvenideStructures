package com.elvenide.structures.doors;

import com.elvenide.core.providers.config.Config;
import com.elvenide.core.providers.config.ConfigSection;
import com.elvenide.structures.StructureManager;
import com.elvenide.structures.doors.types.SlidingDoor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class DoorManager implements Listener, StructureManager<Door> {

    public static Listener creationListener = null;
    public static String creatorName = "";
    public static String creationName = "";

    private final HashMap<String, HashMap<String, Door>> worldDoors = new HashMap<>();

    private @NotNull HashMap<String, Door> getAllMap(World world) {
        if (worldDoors.containsKey(world.getName()))
            return worldDoors.get(world.getName());

        HashMap<String, Door> doors = new HashMap<>();
        Config config = getStructures(world);
        ConfigSection data = config.getSection("doors");

        if (data != null)
            for (String key : data.getKeys()) {
                ConfigSection doorData = data.getSection(key);
                Door door;

                if (doorData == null)
                    continue;

                if (Objects.equals(doorData.getString("type"), "sliding"))
                    door = new SlidingDoor(doorData);
                else
                    continue;

                doors.put(key, door);
            }

        worldDoors.put(world.getName(), doors);
        return worldDoors.getOrDefault(world.getName(), new HashMap<>());
    }

    public double getConfiguredConnectionRange(World world) {
        return getConfiguration(world).getDouble("doors.connection-range");
    }

    public List<String> getNames(World world) {
        return getAllMap(world).keySet().stream().toList();
    }

    public void add(World world, String name, Door door) {
        getAllMap(world).put(name, door);
    }

    @Override
    public @Nullable Door getNearby(Location loc) {
        for (Door door : getAllMap(loc.getWorld()).values()) {
            if (door.isNearby(loc))
                return door;
        }

        return null;
    }

    //// Returns the door that is adjacent to the given door, if any
    public @Nullable Door getAdjacent(Door door) {
        for (Door otherDoor : getAllMap(door.getBlocks().getFirst().initialLocation().getWorld()).values()) {
            if (otherDoor.equals(door) || otherDoor.isMoving() || otherDoor.isOpen() != door.isOpen())
                continue;

            for (DoorBlock otherBlock : otherDoor.getBlocks()) {
               for (DoorBlock block : door.getBlocks()) {
                   if (block.initialLocation().distanceSquared(otherBlock.initialLocation()) <= 1)
                       return otherDoor;
               }
            }
        }

        return null;
    }

    /// Completely removes a door
    public void delete(World world, String name) {
        Door door = getAllMap(world).remove(name);
        door.reset();

        ConfigSection doors = getStructures(world).getSection("doors");
        if (doors == null)
            doors = getStructures(world).createSection("doors");
        doors.setAndSave(name, null);
    }

    /// Checks if a door is moving
    public boolean isMoving(World world, String name) {
        if (!getAllMap(world).containsKey(name))
            return false;
        return getAllMap(world).get(name).isMoving();
    }

    /// Checks if a location is part of a door (in its closed state)
    public boolean isDoorBlock(Location loc) {
        for (Door door : getAllMap(loc.getWorld()).values()) {
            if (door.isDoorBlock(loc))
                return true;
        }

        return false;
    }
}
