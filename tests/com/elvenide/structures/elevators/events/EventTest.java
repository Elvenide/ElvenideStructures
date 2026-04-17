package com.elvenide.structures.elevators.events;

import com.elvenide.structures.elevators.Elevator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class EventTest {

    @Test
    void testElevatorEndEvent() {
        Elevator elevator = mock(Elevator.class);
        ElevatorEndEvent event = new ElevatorEndEvent(elevator);
        assertEquals(elevator, event.elevator());
    }

    @Test
    void testElevatorStartEvent() {
        Elevator elevator = mock(Elevator.class);
        ElevatorStartEvent event = new ElevatorStartEvent(elevator);
        assertEquals(elevator, event.elevator());
    }
}
