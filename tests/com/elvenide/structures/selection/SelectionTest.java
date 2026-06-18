package com.elvenide.structures.selection;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SelectionTest {

    private ServerMock server;
    private WorldMock world;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("test");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testSelection() {
        Location pos1 = new Location(world, 0, 100, 0);
        Location pos2 = new Location(world, 2, 102, 2);

        // Set some blocks to be non-air
        world.getBlockAt(0, 100, 0).setType(Material.STONE);
        world.getBlockAt(1, 101, 1).setType(Material.STONE);
        world.getBlockAt(2, 102, 2).setType(Material.STONE);

        Selection selection = new Selection(pos1, pos2);

        List<Location> locations = new ArrayList<>();
        selection.forEach(locations::add);

        assertEquals(3, locations.size());
        assertTrue(locations.contains(new Location(world, 0, 100, 0)));
        assertTrue(locations.contains(new Location(world, 1, 101, 1)));
        assertTrue(locations.contains(new Location(world, 2, 102, 2)));
        assertEquals(world, selection.getWorld());
    }

    @Test
    void testSelectionEmpty() {
        Location pos1 = new Location(world, 0, 100, 0);
        Location pos2 = new Location(world, 1, 101, 1);

        // All blocks are air by default
        Selection selection = new Selection(pos1, pos2);

        List<Location> locations = new ArrayList<>();
        selection.forEach(locations::add);

        assertEquals(0, locations.size());
        assertEquals(10000, selection.getMinimumY());
    }

    @Test
    void testTertiaryPosition() {
        Location pos1 = new Location(world, 0, 100, 0);
        Location pos2 = new Location(world, 0, 100, 0);
        Selection selection = new Selection(pos1, pos2);

        assertNull(selection.getTertiaryPosition());

        Location tert = new Location(world, 105, 105, 105);
        selection.setTertiaryPosition(tert);

        assertEquals(tert, selection.getTertiaryPosition());
    }

    @Test
    void testMinimumY() {
        Location pos1 = new Location(world, 0, 110, 0);
        Location pos2 = new Location(world, 0, 120, 0);

        world.getBlockAt(0, 115, 0).setType(Material.STONE);
        world.getBlockAt(0, 118, 0).setType(Material.STONE);

        Selection selection = new Selection(pos1, pos2);

        assertEquals(115, selection.getMinimumY());
    }
}
