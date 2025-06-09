package com.elvenide.structures.elevators.events;

import com.elvenide.core.providers.event.CoreCancellable;
import com.elvenide.core.providers.event.CoreEvent;
import com.elvenide.structures.elevators.Elevator;
import org.bukkit.Location;

import java.util.HashSet;

public record ElevatorStartEvent(Elevator elevator) implements CoreEvent, CoreCancellable {

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

}