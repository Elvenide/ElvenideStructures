package com.elvenide.structures.elevators;

import com.elvenide.core.ElvenideCore;
import com.elvenide.core.providers.config.AbstractSection;
import com.elvenide.core.providers.config.Config;
import com.elvenide.structures.elevators.events.ElevatorEndEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class ElevatorManager implements Listener {

    private final HashMap<String, HashMap<String, Elevator>> worldElevators = new HashMap<>();
    private final HashSet<UUID> usersOnCooldown = new HashSet<>();

    private Config getConfig(World world) {
        return ElvenideCore.config.get("../../" + world.getName() + "/elevators.db");
    }

    private List<Elevator> getAll(World world) {
        if (worldElevators.containsKey(world.getName()))
            return worldElevators.get(world.getName()).values().stream().toList();

        HashMap<String, Elevator> elevators = new HashMap<>();
        Config data = getConfig(world);

        for (String key : data.getKeys()) {
            elevators.put(key, new Elevator(data.section(key)));
        }

        worldElevators.put(world.getName(), elevators);
        return elevators.values().stream().toList();
    }

    public List<String> getNames(World world) {
        return getAll(world).stream().map(Elevator::getName).toList();
    }

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
        return worldElevators.getOrDefault(world.getName(), new HashMap<>()).get(name);
    }

    public @NotNull Elevator create(String name, double speed, World world) {
        Config elevators = getConfig(world);
        AbstractSection elevator = elevators.addSection(name);
        elevator.set("speed", speed);
        elevators.save();

        return new Elevator(elevator);
    }

    public void delete(String name, World world) {
        Config elevators = getConfig(world);
        elevators.remove(name);
        elevators.save();

        worldElevators.get(world.getName()).remove(name);
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
                        ElvenideCore.text.send(event.getPlayer(), "<red>That elevator is on cooldown for %.1f seconds.", elevator.getRemainingCooldown());
                    }
                    continue;
                }

                elevator.moveDelayTrigger = event.getPlayer();
                ElvenideCore.tasks.builder()
                    .then(task -> {
                        if (elevator.moveDelayTrigger != event.getPlayer())
                            return;

                        if (elevator.isInside(event.getPlayer()))
                            try {
                                usersOnCooldown.add(event.getPlayer().getUniqueId());
                                elevator.move();
                            } catch (IllegalStateException e) {
                                ElvenideCore.text.send(event.getPlayer(), e.getMessage());
                            }
                        else
                            elevator.moveDelayTrigger = null;
                    })
                    .delay((long) (elevator.getMoveDelay() * 20D));
                break;
            }
        }
    }

    @EventHandler
    public void onElevatorEnd(ElevatorEndEvent event) {
        ElvenideCore.tasks.builder()
            .then(task -> {
                event.getElevator().moveDelayTrigger = null;
                for (LivingEntity e : event.getPassengers())
                    usersOnCooldown.remove(e.getUniqueId());
            })
            .delay(8 * 20);
    }
}
