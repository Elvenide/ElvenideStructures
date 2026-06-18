package com.elvenide.structures.switches;

import com.elvenide.structures.StructureManager;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SwitchListenerTest {

    private ServerMock server;
    private WorldMock world;
    private SwitchListener listener;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("test");
        listener = new SwitchListener();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testPressurePlatePress() {
        Player player = server.addPlayer();
        Location loc = new Location(world, 0, 100, 0);
        Block block = world.getBlockAt(loc);
        block.setType(Material.STONE_PRESSURE_PLATE);

        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.PHYSICAL, null, block, null);
        
        try (MockedStatic<StructureManager> mockedManager = Mockito.mockStatic(StructureManager.class)) {
            listener.onSwitchPress(event);
            mockedManager.verify(() -> StructureManager.useSwitch(player, loc));
        }
    }

    @Test
    void testButtonPress() {
        Player player = server.addPlayer();
        Location loc = new Location(world, 0, 100, 0);
        Block block = world.getBlockAt(loc);
        block.setType(Material.OAK_BUTTON);

        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, null, block, null);
        
        try (MockedStatic<StructureManager> mockedManager = Mockito.mockStatic(StructureManager.class)) {
            listener.onSwitchPress(event);
            // location = location.clone().subtract(0, 1, 0);
            mockedManager.verify(() -> StructureManager.useSwitch(player, loc.clone().subtract(0, 1, 0)));
        }
    }

    @Test
    void testIrrelevantInteraction() {
        Player player = server.addPlayer();
        Location loc = new Location(world, 0, 100, 0);
        Block block = world.getBlockAt(loc);
        block.setType(Material.STONE);

        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.LEFT_CLICK_BLOCK, null, block, null);
        
        try (MockedStatic<StructureManager> mockedManager = Mockito.mockStatic(StructureManager.class)) {
            listener.onSwitchPress(event);
            mockedManager.verify(() -> StructureManager.useSwitch(any(), any()), never());
        }
    }
}
