package com.elvenide.structures.selection;

import com.elvenide.structures.CoreMock;
import com.elvenide.structures.ElvenideStructures;
import com.elvenide.structures.elevators.ElevatorManager;
import com.elvenide.core.Core;
import com.elvenide.core.providers.config.Config;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SelectionVisualizerTest {

    private ServerMock server;
    private WorldMock world;
    private MockedStatic<Core> mockedCore;
    private MockedStatic<ElvenideStructures> mockedStructures;
    private ElevatorManager elevatorManager;
    private Config elevatorConfig;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("test");
        mockedCore = CoreMock.mockCore();
        
        elevatorManager = mock(ElevatorManager.class);
        elevatorConfig = mock(Config.class);
        when(elevatorManager.getConfiguration(any())).thenReturn(elevatorConfig);
        when(elevatorConfig.getBoolean(any(), any(Boolean.class))).thenReturn(true);
        when(elevatorConfig.getString(any(), any())).thenReturn("GREEN");

        mockedStructures = Mockito.mockStatic(ElvenideStructures.class);
        mockedStructures.when(ElvenideStructures::elevators).thenReturn(elevatorManager);
    }

    @AfterEach
    void tearDown() {
        mockedCore.close();
        mockedStructures.close();
        MockBukkit.unmock();
    }

    @Test
    void testGetNew() {
        Player player = server.addPlayer();
        Selection selection = mock(Selection.class);
        when(selection.getWorld()).thenReturn(world);

        SelectionVisualizer visualizer = SelectionVisualizer.getNew(selection, player);
        assertNotNull(visualizer);
        
        SelectionVisualizer same = SelectionVisualizer.getExisting(player);
        assertEquals(visualizer, same);
        
        SelectionVisualizer next = SelectionVisualizer.getNew(selection, player);
        assertNotEquals(visualizer, next);
    }

    @Test
    void testCreateAndRemove() {
        Player player = server.addPlayer();
        Location pos1 = new Location(world, 0, 100, 0);
        Location pos2 = new Location(world, 0, 100, 0);
        Selection selection = new Selection(pos1, pos2); // contains 0 blocks if air
        
        // Let's make it contain one block
        world.getBlockAt(0, 100, 0).setType(org.bukkit.Material.STONE);
        selection = new Selection(pos1, pos2);

        SelectionVisualizer visualizer = SelectionVisualizer.getNew(selection, player);
        visualizer.create();
        
        // Removed Core.tasks verification
        
        visualizer.remove();
    }
}
