package com.elvenide.structures.elevators;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ElevatorBlock {

    private BlockData blockData;
    private FallingBlock block;
    private final Location currentLocation;
    private final Elevator parent;
    private final int baseDifference;
    int targetY;
    private final boolean isFloorBlock;
    public boolean atDestination = false;

    ElevatorBlock(Location startLocation, Elevator parent, boolean isFloorBlock) {
        currentLocation = startLocation.clone();
        this.parent = parent;
        this.baseDifference = startLocation.getBlockY() - parent.getBaseY();
        currentLocation.setY(parent.getCurrentY() + baseDifference);
        this.isFloorBlock = isFloorBlock;
    }

    Location getCurrentLocation() {
        return currentLocation;
    }

    private void setCurrentY(double y) {
        currentLocation.setY(y);
    }

    private Entity blockEntity() {
        return block;
    }

    public void spawn(int targetY) {
        this.blockData = getCurrentLocation().getBlock().getBlockData();
        getCurrentLocation().toBlockLocation().getBlock().setBlockData(Material.AIR.createBlockData(), false);

        Location l = getCurrentLocation().toCenterLocation();
        l.subtract(0, 0.5, 0);
        this.block = getCurrentLocation().getWorld().spawn(l, FallingBlock.class, b -> {
            b.setBlockData(blockData);
            b.shouldAutoExpire(false);
            b.setCancelDrop(true);
            b.setGravity(false);
        });

        atDestination = false;
        this.targetY = targetY + baseDifference;
    }

    public double getBlocksPerTick(double direction) {
        return parent.getSpeed() * direction / 20.0;
    }

    public double move(double direction) {
        double blocksPerTick = getBlocksPerTick(direction);

        double prevY = getCurrentLocation().getY();
        double y = prevY + blocksPerTick;
        if (direction > 0 && y >= targetY) {
            y = targetY;
        } else if (direction < 0 && y <= targetY) {
            y = targetY;
        }

        // Check if at destination
        atDestination = y == targetY;

        double actualMove = y - prevY;

        // Move elevator
        setCurrentY(y);
        blockEntity().setVelocity(new Vector(0, actualMove, 0));

        return actualMove;
    }

    public void end() {
        Location finalLoc = getCurrentLocation().toBlockLocation();
        finalLoc.setY(targetY);
        finalLoc.getBlock().setBlockData(blockData, false);

        if (blockEntity() != null)
            blockEntity().remove();

        // Update current location to exact target
        currentLocation.setY(targetY);
        atDestination = false;
    }

    public int getCurrentY() {
        return (int) Math.round(getCurrentLocation().getY()) - baseDifference;
    }

    public boolean isValid() {
        return blockEntity() != null && blockEntity().isValid();
    }

    public boolean isFloorBlock() {
        return isFloorBlock;
    }

    public List<LivingEntity> getEntitiesOnBlock() {
        Location horizontalCenter = getCurrentLocation().toCenterLocation();
        horizontalCenter.setY(getCurrentLocation().getY() + 0.85);
        return new ArrayList<>(horizontalCenter.getNearbyLivingEntities(0.5, 0.3, 0.5));
    }

    public List<LivingEntity> getEntitiesInsideBlock() {
        Location horizontalCenter = getCurrentLocation().toCenterLocation();
        return new ArrayList<>(horizontalCenter.getNearbyLivingEntities(0.5, 0.5, 0.5));
    }

}
