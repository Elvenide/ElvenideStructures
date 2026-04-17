package com.elvenide.structures.doors.types;

import com.elvenide.core.Core;
import com.elvenide.core.providers.config.ConfigSection;
import com.elvenide.structures.CoreMock;
import com.elvenide.structures.ElvenideStructures;
import com.elvenide.structures.doors.DoorBlock;
import com.elvenide.structures.doors.DoorManager;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Vector3f;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SlidingDoorTest {

    private ServerMock server;
    private WorldMock world;
    private MockedStatic<Core> mockedCore;
    private MockedStatic<ElvenideStructures> mockedStructures;
    private ConfigSection config;
    private DoorManager doorManager;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("test");
        mockedCore = CoreMock.mockCore();
        
        config = mock(ConfigSection.class);
        doorManager = mock(DoorManager.class);
        
        mockedStructures = Mockito.mockStatic(ElvenideStructures.class);
        mockedStructures.when(ElvenideStructures::doors).thenReturn(doorManager);
    }

    @AfterEach
    void tearDown() {
        mockedCore.close();
        mockedStructures.close();
        MockBukkit.unmock();
    }

    @Test
    void testSlidingDoor() {
        Location loc = new Location(world, 0, 100, 0);
        loc.getBlock().setType(Material.STONE);

        ConfigSection locsSection = mock(ConfigSection.class);
        when(config.contains("locations")).thenReturn(true);
        when(config.getSectionOrThrow("locations")).thenReturn(locsSection);
        when(locsSection.getKeys()).thenReturn(new HashSet<>(Collections.singletonList("0_100_0")));
        when(locsSection.getLocation("0_100_0")).thenReturn(loc);

        when(config.getVector("move-direction")).thenReturn(new Vector(0, 1, 0));
        when(config.getInt("move-distance")).thenReturn(2);
        when(config.getFloat("move-duration")).thenReturn(1.0f);

        // TODO: fix this test getting skipped due to below line
        SlidingDoor door = new SlidingDoor(config);

        assertEquals(20, door.getMoveDuration());
        assertEquals(new Vector(0, 1, 0), door.getMoveDirection());
        assertEquals(2, door.getMoveDistance());
        assertEquals(1, door.getBlocks().size());

        DoorBlock block = door.getBlocks().get(0);
        Transformation trans = door.getTransformationAtPercent(block, 0.5);
        assertEquals(new Vector3f(0, 1, 0), trans.getTranslation()); // 0.5 * 2 = 1.0 in Y
    }

    @Test
    void testOpenClose() {
        Location loc = new Location(world, 0, 100, 0);
        loc.getBlock().setType(Material.STONE);

        ConfigSection locsSection = mock(ConfigSection.class);
        when(config.contains("locations")).thenReturn(true);
        when(config.getSectionOrThrow("locations")).thenReturn(locsSection);
        when(locsSection.getKeys()).thenReturn(new HashSet<>(Collections.singletonList("0_100_0")));
        when(locsSection.getLocation("0_100_0")).thenReturn(loc);
        when(config.getBoolean("open", false)).thenReturn(false);

        // TODO: fix this test getting skipped due to below line
        SlidingDoor door = new SlidingDoor(config);

        assertFalse(door.isOpen());
        assertFalse(door.isMoving());

        door.open();
        assertTrue(door.isMoving());
        
        // Wait for tasks?
        // Since we mocked Core.tasks to not run immediately, we can't easily test the full movement here
        // unless we make CoreMock run tasks.
    }
}
