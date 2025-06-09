package com.elvenide.structures.elevators.events;

import com.elvenide.core.providers.event.CoreEvent;
import com.elvenide.structures.elevators.Elevator;
import com.elvenide.structures.elevators.ElevatorBlock;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record ElevatorEndEvent(Elevator elevator) implements CoreEvent {

    public HashSet<Location> getCurrentEntryLocations() {
        return new HashSet<>(
            elevator.getEntryAdjacentBlocks().stream()
                .map(loc -> {
                    Location current = loc.clone();
                    current.setY(elevator.getCurrentY());
                    return current;
                }).toList()
        );
    }

    public Set<LivingEntity> getPassengers() {
        return elevator.getElevatorBlocks().stream().map(ElevatorBlock::getEntitiesOnBlock).flatMap(List::stream).collect(Collectors.toSet());
    }

}
