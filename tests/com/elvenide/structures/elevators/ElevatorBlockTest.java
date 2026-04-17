package com.elvenide.structures.elevators;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ElevatorBlockTest {

    private ServerMock server;
    private WorldMock world;
    private Elevator elevator;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("test");
        elevator = mock(Elevator.class);
        when(elevator.getBaseY()).thenReturn(100);
        when(elevator.getCurrentY()).thenReturn(100);
        when(elevator.getSpeed()).thenReturn(1.0);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testElevatorBlockConstructor() {
        Location start = new Location(world, 0, 100, 0);
        ElevatorBlock eb = new ElevatorBlock(start, elevator, true);
        assertEquals(start, eb.getCurrentLocation());
        assertTrue(eb.isFloorBlock());
        assertEquals(100, eb.getCurrentY());
    }

    @Test
    void testSpawn() {
        Location start = new Location(world, 0, 100, 0);
        start.getBlock().setType(Material.STONE);
        ElevatorBlock eb = new ElevatorBlock(start, elevator, true);
        
        eb.spawn(110);
        
        assertEquals(Material.AIR, start.getBlock().getType());
        assertNotNull(eb.getCurrentLocation());
        assertTrue(eb.isValid());
        assertEquals(110, eb.targetY);
    }

    @Test
    void testMoveUp() {
        Location start = new Location(world, 0, 100, 0);
        start.getBlock().setType(Material.STONE);
        ElevatorBlock eb = new ElevatorBlock(start, elevator, true);
        eb.spawn(105); // Target 105

        // Speed is 1.0, direction 1, tick 1/20 = 0.05
        double move = eb.move(1);
        assertEquals(0.05, move, 0.001);
        assertEquals(100.05, eb.getCurrentLocation().getY(), 0.001);
        assertFalse(eb.atDestination);
    }

    @Test
    void testMoveAtDestination() {
        Location start = new Location(world, 0, 100, 0);
        start.getBlock().setType(Material.STONE);
        ElevatorBlock eb = new ElevatorBlock(start, elevator, true);
        eb.spawn(101); // Target 101

        when(elevator.getSpeed()).thenReturn(20.0); // 1 block per tick
        
        double move = eb.move(1);
        assertEquals(1.0, move, 0.001);
        assertEquals(101.0, eb.getCurrentLocation().getY(), 0.001);
        assertTrue(eb.atDestination);
    }

    @Test
    void testEnd() {
        Location start = new Location(world, 0, 100, 0);
        start.getBlock().setType(Material.STONE);
        ElevatorBlock eb = new ElevatorBlock(start, elevator, true);
        eb.spawn(110);
        
        eb.end();
        
        assertEquals(Material.STONE, world.getBlockAt(0, 110, 0).getType());
        assertFalse(eb.isValid());
    }
}
