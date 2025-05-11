package com.elvenide.structures.selection;

import com.elvenide.core.Core;
import com.elvenide.structures.Keys;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class SelectionTool {

    public ItemStack create() {
        return Core.items.builder(Material.NETHERITE_AXE)
            .name("<smooth_purple>Elvenide's Selection Tool")
            .data(Keys.SELECTION_TOOL, PersistentDataType.BOOLEAN, true)
            .lore("<yellow>Left-click <gray>to select corner #1")
            .lore("<yellow>Right-click <gray>to select corner #2")
            .lore("<yellow>Shift-click <gray>to complete selection")
            .build();
    }

    public static boolean is(ItemStack item) {
        return Core.items.hasData(item, Keys.SELECTION_TOOL);
    }

    /// Returns false if the corner is already selected
    public static boolean selectCorner1(ItemStack item, Location corner1) {
        Location prevCorner1 = getCorner1(item, corner1.getWorld());
        if (prevCorner1 != null && prevCorner1.equals(corner1))
            return false;

        Core.items.builder(item)
            .data(Keys.SELECTION_CORNER_1, PersistentDataType.INTEGER_ARRAY, new int[] { corner1.getBlockX(), corner1.getBlockY(), corner1.getBlockZ() });

        return true;
    }

    /// Returns false if the corner is already selected
    public static boolean selectCorner2(ItemStack item, Location corner2) {
        Location prevCorner2 = getCorner2(item, corner2.getWorld());
        if (prevCorner2 != null && prevCorner2.equals(corner2))
            return false;

        Core.items.builder(item)
            .data(Keys.SELECTION_CORNER_2, PersistentDataType.INTEGER_ARRAY, new int[] { corner2.getBlockX(), corner2.getBlockY(), corner2.getBlockZ() });

        return true;
    }

    private static Location getCorner1(ItemStack item, World world) {
        if (!Core.items.hasData(item, Keys.SELECTION_CORNER_1))
            return null;

        int[] corner1Coords = Core.items.getData(item, Keys.SELECTION_CORNER_1, PersistentDataType.INTEGER_ARRAY);
        return new Location(world, corner1Coords[0], corner1Coords[1], corner1Coords[2]);
    }

    private static Location getCorner2(ItemStack item, World world) {
        if (!Core.items.hasData(item, Keys.SELECTION_CORNER_2))
            return null;

        int[] corner2Coords = Core.items.getData(item, Keys.SELECTION_CORNER_2, PersistentDataType.INTEGER_ARRAY);
        return new Location(world, corner2Coords[0], corner2Coords[1], corner2Coords[2]);
    }

    public static @Nullable Selection getSelection(ItemStack item, World world) {
        Location corner1 = getCorner1(item, world);
        Location corner2 = getCorner2(item, world);

        if (corner1 == null || corner2 == null)
            return null;

        return new Selection(corner1, corner2);
    }

}
