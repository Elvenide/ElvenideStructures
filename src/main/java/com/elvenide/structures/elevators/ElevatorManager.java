package com.elvenide.structures.elevators;

import com.elvenide.core.Core;
import com.elvenide.core.providers.config.ConfigSection;
import com.elvenide.core.providers.config.Config;
import com.elvenide.core.providers.event.CoreEventHandler;
import com.elvenide.core.providers.event.CoreEventPriority;
import com.elvenide.core.providers.event.CoreListener;
import com.elvenide.structures.ElvenideStructures;
import com.elvenide.structures.StructureManager;
import com.elvenide.structures.doors.Door;
import com.elvenide.structures.elevators.events.ElevatorEndEvent;
import com.elvenide.structures.elevators.events.ElevatorStartEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ElevatorManager implements Listener, CoreListener, StructureManager<Elevator> {

    private final HashMap<String, HashMap<String, Elevator>> worldElevators = new HashMap<>();
    private final HashSet<UUID> usersOnCooldown = new HashSet<>();

    private @NotNull List<Elevator> getAll(World world) {
        if (worldElevators.containsKey(world.getName()))
            return worldElevators.get(world.getName()).values().stream().toList();

        HashMap<String, Elevator> elevators = new HashMap<>();
        Config config = getConfig(world);
        ConfigSection data = config.getSection("elevators");

        if (data != null)
            for (String key : data.getKeys()) {
                elevators.put(key, new Elevator(data.getSection(key)));
            }

        worldElevators.put(world.getName(), elevators);
        return elevators.values().stream().toList();
    }

    private @NotNull HashMap<String, Elevator> getAllMap(World world) {
        getAll(world);
        return worldElevators.getOrDefault(world.getName(), new HashMap<>());
    }

    public List<String> getNames(World world) {
        return getAllMap(world).keySet().stream().toList();
    }

    @Override
    public @Nullable Elevator getNearby(Location loc) {
        List<Elevator> elevators = getAll(loc.getWorld());

        for (Elevator elevator : elevators) {
            if (elevator.isNearby(loc)) {
                return elevator;
            }
        }

        return null;
    }

    public @Nullable Elevator get(String name, World world) {
        return getAllMap(world).get(name);
    }

    public @NotNull Elevator create(String name, double speed, World world) {
        Config config = getConfig(world);
        ConfigSection elevators = config.getSection("elevators");
        if (elevators == null)
            elevators = config.createSection("elevators");

        ConfigSection elevator = elevators.getSection(name);
        elevator.set("speed", speed);
        config.save();

        Elevator e = new Elevator(elevator);
        worldElevators.getOrDefault(world.getName(), new HashMap<>()).put(name, e);
        return e;
    }

    public void delete(String name, World world) {
        Config config = getConfig(world);
        ConfigSection elevators = config.getSection("elevators");
        if (elevators == null)
            return;

        elevators.set(name, null);
        config.save();

        getAllMap(world).remove(name);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.hasChangedPosition())
            return;

        for (Elevator elevator : getAll(event.getPlayer().getWorld())) {
            if (elevator.isMoving())
                continue;

            if (elevator.moveDelayTrigger != null)
                continue;

            if (elevator.isInside(event.getPlayer())) {
                if (elevator.isOnCooldown()) {
                    if (!usersOnCooldown.contains(event.getPlayer().getUniqueId())) {
                        usersOnCooldown.add(event.getPlayer().getUniqueId());
                        Core.text.send(event.getPlayer(), "<red>✖ That elevator is on cooldown for %.1f seconds.", elevator.getRemainingCooldown());
                    }
                    continue;
                }

                elevator.moveDelayTrigger = event.getPlayer();
                Core.text.send(event.getPlayer(), "<smooth_blue>ℹ Elevator will depart in %.1f seconds...", elevator.getMoveDelay());

                Core.tasks.builder()
                    .then(task -> {
                        if (elevator.moveDelayTrigger != event.getPlayer())
                            return;

                        if (elevator.isInside(event.getPlayer()))
                            try {
                                usersOnCooldown.add(event.getPlayer().getUniqueId());
                                elevator.move();
                            } catch (IllegalStateException e) {
                                Core.text.send(event.getPlayer(), e.getMessage());
                                elevator.moveDelayTrigger = null;
                            }
                        else
                            elevator.moveDelayTrigger = null;
                    })
                    .delay((long) (elevator.getMoveDelay() * 20D));
                break;
            }
        }
    }

    @CoreEventHandler(priority = CoreEventPriority.EARLIEST)
    public void onElevatorEnd(ElevatorEndEvent event) {
        // Find a door near any of the target-level entry locations
        @Nullable Door door = event.getCurrentEntryLocations().stream()
            .map(loc -> ElvenideStructures.doors().getNearby(loc))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);

        // Open the door and any adjacent door, if found
        if (door != null && !door.isOpen()) {
            Door adjacent = ElvenideStructures.doors().getAdjacent(door);
            door.open();
            if (adjacent != null)
                adjacent.open();
        }

        // Wait at least 8 seconds to end move delay trigger and resume cooldown messages
        Core.tasks.builder()
            .then(task -> {
                event.elevator().moveDelayTrigger = null;
                for (LivingEntity e : event.getPassengers())
                    usersOnCooldown.remove(e.getUniqueId());
            })
            .delay(8 * 20 + (door != null ? door.getMoveDuration() : 0L));
    }

    @CoreEventHandler(priority = CoreEventPriority.LATEST, ignoreCancelled = true)
    public void onElevatorStart(ElevatorStartEvent event) {
        // Find a door near any of the base-level entry locations
        @Nullable Door door = event.getCurrentEntryLocations().stream()
            .map(loc -> ElvenideStructures.doors().getNearby(loc))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);

        // Ignore if no door found or if door is closed
        if (door == null || !door.isOpen())
            return;

        // Freeze all players on the elevator
        event.getPassengers().forEach(le -> {
            if (le instanceof Player p) {
                p.setAllowFlight(true);
                p.setFlySpeed(0);
                p.setFlying(true);
                p.setWalkSpeed(0);
            }
            else
                le.setGravity(false);
        });

        // Close the door and any adjacent door
        Door adjacent = ElvenideStructures.doors().getAdjacent(door);
        door.close();
        if (adjacent != null)
            adjacent.close();

        // Wait until door closed, then run elevator again
        // (We can safely ignore the cooldown, as the cooldown must be over to reach this point)
        Core.tasks.builder()
            .then(task -> {
                event.elevator().move(true);
            })
            .delay(door.getMoveDuration() + 5L);

        // Cancel event
        event.setCancelled(true);
    }
}
