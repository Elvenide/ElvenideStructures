package com.elvenide.structures.elevators;

import com.elvenide.structures.ElvenideStructures;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class ElevatorUtils {

    /// If a block is not passable
    public static boolean isSolid(Block block) {
        // Do not consider barriers to be solid
        if (block.getType() == Material.BARRIER)
            return false;

        // Do not consider custom door blocks to be solid
        if (ElvenideStructures.doors().isDoorBlock(block.getLocation()))
            return false;

        return !block.isPassable();
    }

    /// If a block is either passable or can be opened to pass through
    public static boolean isWalkable(Block block) {
        // Do not allow walking in structure voids
        if (block.getType() == Material.STRUCTURE_VOID)
            return false;

        // Support passables, and various door types
        return block.isPassable()
            || Tag.DOORS.isTagged(block.getType())
            || Tag.FENCE_GATES.isTagged(block.getType())
            || Tag.TRAPDOORS.isTagged(block.getType())
            // Custom doors can be opened and walked through:
            || ElvenideStructures.doors().isDoorBlock(block.getLocation());
    }

    /// Freezes movement of entity
    public static void freezePassengerMovement(LivingEntity e) {
        if (e instanceof Player p) {
            p.setAllowFlight(true);
            p.setFlySpeed(0);
            p.setWalkSpeed(0);
            p.setFlying(true);
        }
        else e.setGravity(false);
    }

    /// Unfreezes movement of entity
    public static void unfreezePassengerMovement(LivingEntity e) {
        if (e instanceof Player p) {
            p.setFlying(false);
            p.setFlySpeed(0.1f);
            p.setWalkSpeed(0.2f);
            if (p.getGameMode() != GameMode.CREATIVE)
                p.setAllowFlight(false);
        }
        else e.setGravity(true);
    }

}
