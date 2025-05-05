package com.elvenide.structures.elevators.events;

import com.elvenide.structures.elevators.Elevator;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ElevatorEndEvent extends ElevatorEvent {
    private static final HandlerList handlers = new HandlerList();

    public ElevatorEndEvent(Elevator elevator) {
        super(elevator);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
