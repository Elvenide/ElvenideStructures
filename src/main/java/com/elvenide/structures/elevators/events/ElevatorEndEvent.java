package com.elvenide.structures.elevators.events;

import com.elvenide.core.providers.event.CoreEvent;
import com.elvenide.structures.elevators.Elevator;

public record ElevatorEndEvent(Elevator elevator) implements CoreEvent {
}
