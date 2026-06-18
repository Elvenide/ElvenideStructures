package com.elvenide.structures.elevators;

import com.elvenide.core.Core;
import com.elvenide.core.providers.config.ConfigSection;
import com.elvenide.structures.CoreMock;
import com.elvenide.structures.ElvenideStructures;
import com.elvenide.structures.doors.DoorManager;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ElevatorTest {

    private ServerMock server;
    private WorldMock world;
    private MockedStatic<Core> mockedCore;
    private MockedStatic<ElvenideStructures> mockedStructures;
    private ConfigSection config;
    private DoorManager doorManager;
    private ElevatorManager elevatorManager;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("test");
        mockedCore = CoreMock.mockCore();
        
        config = mock(ConfigSection.class);
        doorManager = mock(DoorManager.class);
        elevatorManager = mock(ElevatorManager.class);
        
        mockedStructures = Mockito.mockStatic(ElvenideStructures.class);
        mockedStructures.when(ElvenideStructures::doors).thenReturn(doorManager);
        mockedStructures.when(ElvenideStructures::elevators).thenReturn(elevatorManager);
    }

    @AfterEach
    void tearDown() {
        mockedCore.close();
        mockedStructures.close();
        MockBukkit.unmock();
    }

    @Test
    void testElevatorBasicGetters() {
        when(config.getInt("base-level")).thenReturn(100);
        when(config.getInt("current-level", 100)).thenReturn(100);
        when(config.getInt("dest-level")).thenReturn(110);
        when(config.getDouble("speed", 1)).thenReturn(1.0);
        when(config.getName()).thenReturn("test-elevator");

        Elevator elevator = new Elevator(config);
        
        assertEquals(100, elevator.getBaseY());
        assertEquals(100, elevator.getCurrentY());
        assertEquals(110, elevator.getDestinationY());
        assertEquals(1.0, elevator.getSpeed());
        assertEquals("test-elevator", elevator.getName());
    }

    @Test
    void testMove() {
        when(config.getInt("base-level")).thenReturn(100);
        when(config.getInt("current-level", 100)).thenReturn(100);
        when(config.getInt("dest-level")).thenReturn(110);
        when(config.getDouble("speed", 1)).thenReturn(1.0);
        
        // Mock carriage locations
        ConfigSection locsSection = mock(ConfigSection.class);
        when(config.contains("locations")).thenReturn(true);
        when(config.getSectionOrThrow("locations")).thenReturn(locsSection);
        when(locsSection.getKeys()).thenReturn(new HashSet<>(Collections.singletonList("0_100_0")));
        Location loc = new Location(world, 0, 100, 0);
        loc.getBlock().setType(Material.STONE);
        when(locsSection.getLocation("0_100_0")).thenReturn(loc);

        Elevator elevator = new Elevator(config);
        
        assertFalse(elevator.isMoving());
        elevator.move();
        assertTrue(elevator.isMoving());
    }

    @Test
    void testIsNearby() {
        when(config.getInt("base-level")).thenReturn(100);
        when(config.getInt("current-level", 100)).thenReturn(100);
        when(config.getInt("dest-level")).thenReturn(110);
        
        ConfigSection locsSection = mock(ConfigSection.class);
        when(config.contains("locations")).thenReturn(true);
        when(config.getSectionOrThrow("locations")).thenReturn(locsSection);
        when(locsSection.getKeys()).thenReturn(new HashSet<>(Collections.singletonList("0_100_0")));
        Location loc = new Location(world, 0, 100, 0);
        loc.getBlock().setType(Material.STONE);
        when(locsSection.getLocation("0_100_0")).thenReturn(loc);
        
        when(elevatorManager.getConfiguredConnectionRange(any())).thenReturn(5.0);

        Elevator elevator = new Elevator(config);
        
        assertTrue(elevator.isNearby(new Location(world, 0, 100, 0)));
        assertFalse(elevator.isNearby(new Location(world, 10, 100, 10)));
    }
}
