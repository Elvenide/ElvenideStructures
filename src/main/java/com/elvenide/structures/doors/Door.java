package com.elvenide.structures.doors;

import com.elvenide.core.Core;
import com.elvenide.structures.ElvenideStructures;
import com.elvenide.structures.Structure;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Range;

import java.util.Collection;
import java.util.List;

public interface Door extends Structure {
    /// Creates a movable block for a door
    static DoorBlock createBlock(Location loc, BlockData data) {
        Collection<BlockDisplay> displays = loc.getNearbyEntitiesByType(BlockDisplay.class, 0.5);
        BlockDisplay display;
        if (displays.isEmpty())
            display = loc.getWorld().spawn(loc, BlockDisplay.class, bd -> {
                bd.setBlock(data);
                bd.setVisibleByDefault(false);

                // Initialize interpolation
                bd.setInterpolationDelay(0);
                bd.setInterpolationDuration(0);
            });
        else
            display = displays.iterator().next();

        return new DoorBlock(loc, display);
    }

    /// Returns the total number of ticks the door should take to change state
    int getMoveDuration();

    /// Returns the blocks that make up the door (made with {@link #createBlock(Location, BlockData)})
    List<DoorBlock> getBlocks();

    /// Returns the transformation of the given door block at the given open percent
    Transformation getTransformationAtPercent(DoorBlock block, double percent);

    /// Returns whether the door is currently open, based on config value
    boolean isOpen();

    /// Sets whether the door is currently open, in the config
    void setOpen(boolean open);

    /// Returns whether the door is currently moving
    boolean isMoving();

    /// Sets whether the door is currently moving
    void setMoving(boolean moving);

    /// Returns whether the given location is a door block
    default boolean isDoorBlock(Location loc) {
        for (DoorBlock block : getBlocks()) {
            if (block.initialLocation().equals(loc))
                return true;
        }

        return false;
    }

    /// Deletes all block displays in the door and replaces them with the initial blocks
    default void reset() {
        for (DoorBlock block : getBlocks()) {
            block.initialLocation().getBlock().setBlockData(block.display().getBlock());
            block.display().remove();
        }
    }

    /// Moves all blocks in the door to the given open percent
    default void moveToPercent(double percent) {
        for (DoorBlock block : getBlocks()) {
            block.display().setInterpolationDelay(0);
            block.display().setInterpolationDuration(getMoveDuration());
            block.display().setTransformation(getTransformationAtPercent(block, percent));
        }
    }

    private void move(@Range(from = -1, to = 1) int direction) {
        double percent = direction < 0 ? 0.0 : 1.0;

        // Remove blocks at 0%, and make block displays visible
        for (DoorBlock block : getBlocks()) {
            block.initialLocation().getBlock().setType(Material.AIR);
            block.display().setVisibleByDefault(true);
        }

        // A few tick delay is necessary due to setVisibleByDefault acting like spawning an entity on client,
        // and interpolation can only be set a few ticks after spawning
        Core.tasks.create(t -> {
            // Reset interpolation
            for (DoorBlock block : getBlocks()) {
                block.display().setInterpolationDelay(0);
                block.display().setInterpolationDuration(0);
                block.display().setTransformation(block.display().getTransformation());
            }

            // Move door
            moveToPercent(percent);
            Core.tasks.create(task -> {
                setMoving(false);

                if (percent >= 1)
                    setOpen(true);

                if (percent <= 0) {
                    setOpen(false);
                    for (DoorBlock block : getBlocks()) {
                        block.initialLocation().getBlock().setBlockData(block.display().getBlock(), false);
                        block.display().setVisibleByDefault(false);
                    }
                }
            }).delay(getMoveDuration());
        }).delay(2L);
    }

    /// Fully opens the door over the move duration
    default void open() {
        if (isMoving() || isOpen())
            return;

        setMoving(true);
        move(1);
    }

    /// Fully closes the door over the move duration
    default void close() {
        if (isMoving() || !isOpen())
            return;

        setMoving(true);
        move(-1);
    }

    /// Opens or closes the door, depending on its current state
    default void toggle() {
        if (isOpen())
            close();
        else
            open();
    }

    @Override
    default void onNearbySwitchUsed(Player user, Location loc) {
        Door adjacent = ElvenideStructures.doors().getAdjacent(this);
        toggle();
        if (adjacent != null)
            adjacent.toggle();
    }

    @Override
    default boolean isNearby(Location loc) {
        double range = ElvenideStructures.doors().getConfiguredConnectionRange(loc.getWorld());

        for (DoorBlock baseBlock : getBlocks()) {
            Location adjustedBaseBlock = baseBlock.initialLocation().clone();
            if (loc.distance(adjustedBaseBlock) <= range + 0.5)
                return true;
        }

        return false;
    }
}
