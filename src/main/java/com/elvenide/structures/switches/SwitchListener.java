package com.elvenide.structures.switches;

import com.elvenide.core.Core;
import com.elvenide.structures.StructureManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Switch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class SwitchListener implements Listener {

    @EventHandler
    public void onSwitchPress(PlayerInteractEvent event) {
        // Ensure a block is involved
        Block block = event.getClickedBlock();
        if (block == null)
            return;

        // Ensure the block is a switch (e.g. pressure plate, tripwire, button, or lever)
        Location location = block.getLocation();
        switch (event.getAction()) {
            case PHYSICAL -> {
                if (!Tag.PRESSURE_PLATES.isTagged(block.getType()) && block.getType() != Material.TRIPWIRE)
                    return;

                Core.text.send(event.getPlayer(), "<gold>Plate switch pressed.");
            }
            case RIGHT_CLICK_BLOCK -> {
                BlockData data = block.getBlockData();
                if (!(data instanceof Switch s))
                    return;

                // If the button is already pressed or the lever is already on, ignore
                if (s.isPowered())
                    return;

                Core.text.send(event.getPlayer(), "<light_purple>Hand switch pressed.");
                location = location.clone().subtract(0, 1, 0);
            }
            default -> {
                return;
            }
        }

        // If the block is near a structure, run the structure's functionality
        StructureManager.useSwitch(event.getPlayer(), location);
    }

}
