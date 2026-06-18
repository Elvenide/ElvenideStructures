package com.elvenide.structures.doors;

import com.elvenide.core.Core;
import com.elvenide.core.providers.config.Config;
import com.elvenide.core.providers.config.ConfigSection;
import com.elvenide.structures.CoreMock;
import com.elvenide.structures.ElvenideStructures;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DoorManagerTest {

    private ServerMock server;
    private WorldMock world;
    private MockedStatic<Core> mockedCore;
    private DoorManager manager;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("test");
        mockedCore = CoreMock.mockCore();
        manager = new DoorManager();
    }

    @AfterEach
    void tearDown() {
        mockedCore.close();
        MockBukkit.unmock();
    }

    @Test
    void testGetNearby() {
        Location loc = new Location(world, 0, 100, 0);
        
        Door door = mock(Door.class);
        when(door.isNearby(loc)).thenReturn(true);
        
        manager.add(world, "test-door", door);
        
        assertEquals(door, manager.getNearby(loc));
    }

    @Test
    void testGetNames() {
        assertTrue(manager.getNames(world).isEmpty());
        
        Door door = mock(Door.class);
        manager.add(world, "test-door", door);
        
        assertEquals(1, manager.getNames(world).size());
        assertTrue(manager.getNames(world).contains("test-door"));
    }
}
