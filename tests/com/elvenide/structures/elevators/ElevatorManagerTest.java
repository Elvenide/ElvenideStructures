package com.elvenide.structures.elevators;

import com.elvenide.core.Core;
import com.elvenide.structures.CoreMock;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;

class ElevatorManagerTest {

    private ServerMock server;
    private WorldMock world;
    private MockedStatic<Core> mockedCore;
    private ElevatorManager manager;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("test");
        mockedCore = CoreMock.mockCore();
        manager = new ElevatorManager();
    }

    @AfterEach
    void tearDown() {
        mockedCore.close();
        MockBukkit.unmock();
    }

    @Test
    void testCreate() {
        // We use the real Core.config but it points to a temp directory set up in CoreMock
        Elevator e = manager.create("test", 1.0, 5.0, world);
        assertNotNull(e);
        assertEquals("test", e.getName());
        assertEquals(1.0, e.getSpeed());
    }
}
