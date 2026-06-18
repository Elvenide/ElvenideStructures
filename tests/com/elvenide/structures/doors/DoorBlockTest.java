package com.elvenide.structures.doors;

import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class DoorBlockTest {

    @Test
    void testDoorBlock() {
        Location loc = mock(Location.class);
        BlockDisplay display = mock(BlockDisplay.class);
        DoorBlock doorBlock = new DoorBlock(loc, display);

        assertEquals(loc, doorBlock.initialLocation());
        assertEquals(display, doorBlock.display());
    }
}
