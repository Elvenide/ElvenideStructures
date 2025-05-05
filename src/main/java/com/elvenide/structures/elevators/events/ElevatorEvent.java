package com.elvenide.structures.elevators.events;

import com.elvenide.structures.elevators.Elevator;
import com.elvenide.structures.elevators.ElevatorBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ElevatorEvent extends Event {
    private final Elevator elevator;
    private final Set<LivingEntity> passengers;

    protected ElevatorEvent(Elevator elevator) {
        this.elevator = elevator;
        this.passengers = elevator.getElevatorBlocks().stream().map(ElevatorBlock::getEntitiesOnBlock).flatMap(List::stream).collect(Collectors.toSet());
    }

    public Elevator getElevator() {
        return elevator;
    }

    public Set<LivingEntity> getPassengers() {
        return passengers;
    }
}
