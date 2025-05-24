package com.elvenide.structures.selection.tools;

import com.elvenide.core.Core;
import com.elvenide.core.api.PublicAPI;
import com.elvenide.core.providers.item.ItemBuilder;
import com.elvenide.structures.selection.SelectionTool;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SlidingDoorSelectionTool extends SelectionTool {

    private @Nullable BlockFace slideDirection = null;

    @Override
    protected ItemBuilder create(ItemBuilder builder) {
        return builder.lore("<yellow>Shift-click <gray>to set door sliding direction to your facing direction.");
    }

    @Override
    protected @Nullable String getCompletionError() {
        if (slideDirection == null)
            return "<red>Cannot complete selection; door sliding direction must be set.<br><red>Use <yellow>shift-click</yellow> to set it.";

        return super.getCompletionError();
    }

    @Override
    protected void onShiftLeftClick(Player player) {
        setSlideDirection(player);
    }

    @Override
    protected void onShiftRightClick(Player player) {
        setSlideDirection(player);
    }

    private void setSlideDirection(Player player) {
        Vector dir = player.getEyeLocation().getDirection();
        double absY = Math.abs(dir.getY());
        if (absY > 0.7)
            slideDirection = dir.getY() > 0 ? BlockFace.UP : BlockFace.DOWN;
        else
            slideDirection = player.getFacing();

        // Slide direction should be UP, DOWN, NORTH, SOUTH, EAST, WEST
        // If it is any other option, set it to the closest axis
        switch (slideDirection) {
            case NORTH_EAST, NORTH_NORTH_EAST, NORTH_NORTH_WEST -> slideDirection = BlockFace.NORTH;
            case SOUTH_EAST, SOUTH_SOUTH_EAST, SOUTH_SOUTH_WEST -> slideDirection = BlockFace.SOUTH;
            case SOUTH_WEST, WEST_SOUTH_WEST, WEST_NORTH_WEST -> slideDirection = BlockFace.WEST;
            case NORTH_WEST, EAST_NORTH_EAST, EAST_SOUTH_EAST -> slideDirection = BlockFace.EAST;
        }

        Core.text.send(player, "<aqua>Door sliding direction set to <dark_aqua>{}.", slideDirection.name());
    }

    @PublicAPI
    public @NotNull Vector getMoveDirection() {
        assert slideDirection != null;
        return slideDirection.getDirection();
    }

    @PublicAPI
    public int getMoveDistance() {
        Vector direction = getMoveDirection();
        assert corner1 != null && corner2 != null;

        if (Math.abs(direction.getX()) > 0.1)
            return Math.abs(corner1.getBlockX() - corner2.getBlockX()) + 1;

        if (Math.abs(direction.getY()) > 0.1)
            return Math.abs(corner1.getBlockY() - corner2.getBlockY()) + 1;

        if (Math.abs(direction.getZ()) > 0.1)
            return Math.abs(corner1.getBlockZ() - corner2.getBlockZ()) + 1;

        return 0;
    }

}
