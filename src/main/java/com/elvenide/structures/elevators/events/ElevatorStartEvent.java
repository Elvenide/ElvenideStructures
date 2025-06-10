package com.elvenide.structures.elevators.events;

import com.elvenide.core.providers.event.CoreCancellable;
import com.elvenide.core.providers.event.CoreEvent;
import com.elvenide.structures.elevators.Elevator;

public record ElevatorStartEvent(Elevator elevator) implements CoreEvent, CoreCancellable {
}