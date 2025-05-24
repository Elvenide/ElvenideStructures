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

import java.util.List;

public class SlidingDoorSelectionTool extends SelectionTool {

    private @Nullable BlockFace slideDirection = null;

    @Override
    protected ItemBuilder create(ItemBuilder builder) {
        return builder.lore("<yellow>Shift-click <gray>to change door sliding direction.");
    }

    @Override
    protected @Nullable String getCompletionError() {
        if (slideDirection == null)
            return "<red>Cannot complete selection; door sliding direction must be set.<br><red>Use <yellow>shift-click</yellow> to set it.";

        return super.getCompletionError();
    }

    @Override
    protected void onShiftLeftClick(Player player) {
        cycleSlideDirection(player, false);
    }

    @Override
    protected void onShiftRightClick(Player player) {
        cycleSlideDirection(player, true);
    }

    private void cycleSlideDirection(Player player, boolean forward) {
        List<BlockFace> axes = List.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN);

        if (slideDirection == null)
            slideDirection = axes.getFirst();
        else
            slideDirection = axes.get((axes.indexOf(slideDirection) + (forward ? 1 : -1)) % axes.size());

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
