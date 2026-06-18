package com.elvenide.structures.elevators;

import com.elvenide.structures.ElvenideStructures;
import com.elvenide.structures.doors.DoorManager;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ElevatorUtilsTest {

    private ServerMock server;
    private WorldMock world;
    private DoorManager doorManager;
    private MockedStatic<ElvenideStructures> mockedStructures;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("test");
        doorManager = mock(DoorManager.class);
        mockedStructures = Mockito.mockStatic(ElvenideStructures.class);
        mockedStructures.when(ElvenideStructures::doors).thenReturn(doorManager);
    }

    @AfterEach
    void tearDown() {
        if (mockedStructures != null) {
            mockedStructures.close();
        }
        MockBukkit.unmock();
    }

    @Test
    void testIsSolid() {
        Block stone = world.getBlockAt(0, 100, 0);
        stone.setType(Material.STONE);
        assertTrue(ElevatorUtils.isSolid(stone));

        Block barrier = world.getBlockAt(1, 100, 0);
        barrier.setType(Material.BARRIER);
        assertFalse(ElevatorUtils.isSolid(barrier));

        Block grass = world.getBlockAt(3, 100, 0);
        grass.setType(Material.TALL_GRASS);
        assertFalse(ElevatorUtils.isSolid(grass));

        Block doorBlock = world.getBlockAt(4, 100, 0);
        doorBlock.setType(Material.STONE);
        when(doorManager.isDoorBlock(doorBlock.getLocation())).thenReturn(true);
        assertFalse(ElevatorUtils.isSolid(doorBlock));
    }

    @Test
    void testIsWalkable() {
        Block air = world.getBlockAt(0, 100, 0);
        air.setType(Material.AIR);
        assertTrue(ElevatorUtils.isWalkable(air));

        Block stone = world.getBlockAt(1, 100, 0);
        stone.setType(Material.STONE);
        assertFalse(ElevatorUtils.isWalkable(stone));

        Block oakDoor = world.getBlockAt(3, 100, 0);
        oakDoor.setType(Material.OAK_DOOR);
        assertTrue(ElevatorUtils.isWalkable(oakDoor));

        Block doorBlock = world.getBlockAt(6, 100, 0);
        doorBlock.setType(Material.STONE);
        when(doorManager.isDoorBlock(doorBlock.getLocation())).thenReturn(true);
        assertTrue(ElevatorUtils.isWalkable(doorBlock));
    }

    @Test
    void testFreezeUnfreezePassengerMovement() {
        Player player = server.addPlayer();
        
        ElevatorUtils.freezePassengerMovement(player);
        assertTrue(player.getAllowFlight());
        assertTrue(player.isFlying());

        ElevatorUtils.unfreezePassengerMovement(player);
        assertFalse(player.isFlying());
    }
}
