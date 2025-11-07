package com.elvenide.structures.elevators;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ElevatorBlock {

    private BlockData blockData;
    private FallingBlock block;
    private final Location currentLocation;
    private final Elevator parent;
    private final HashSet<LivingEntity> passengers = new HashSet<>();
    private final int baseDifference;
    private int targetY;
    public boolean atDestination = false;

    ElevatorBlock(Location startLocation, Elevator parent) {
        currentLocation = startLocation.clone();
        this.parent = parent;
        this.baseDifference = startLocation.getBlockY() - parent.getBaseY();
        currentLocation.setY(parent.getCurrentY() + baseDifference);
    }

    private Location getCurrentLocation() {
        return currentLocation;
    }

    private void setCurrentY(double y) {
        currentLocation.setY(y);
    }

    private Entity blockEntity() {
        return block;
    }

    public void spawn(int targetY) {
        passengers.clear();

        if (isBaseBlock())
            for (LivingEntity e : getEntitiesOnBlock()) {
                parent.freezePassengerMovement(e);
                passengers.add(e);
            }

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

    public void move(double direction) {
        double blocksPerTick = getBlocksPerTick(direction);

        double prevY = getCurrentLocation().getY();
        double y = prevY + blocksPerTick;
        if ((prevY < targetY && y > targetY) || (prevY > targetY && y < targetY)) {
            // Reset y
            y = targetY;
        }

        // Check if at destination
        atDestination = y == targetY;

        // Move elevator
        setCurrentY(y);
        blockEntity().setVelocity(new Vector(0, blocksPerTick, 0));

        // Move any entities on elevator
        if (isBaseBlock()) {
            // Get new passengers and disable their movement/gravity
            HashSet<LivingEntity> newPassengers = new HashSet<>(getEntitiesOnBlock());
            for (LivingEntity e : newPassengers) {
                if (!passengers.contains(e)) {
                    parent.freezePassengerMovement(e);
                }
            }

            // Remove old passengers that are too far away
            HashSet<LivingEntity> toRemove = new HashSet<>();
            passengers.forEach(e -> {
                if (e.getLocation().getWorld() != getCurrentLocation().getWorld()
                    || e.getLocation().distanceSquared(getCurrentLocation()) > 3 * 3) {
                    toRemove.add(e);
                    parent.unfreezePassengerMovement(e);
                }
            });
            passengers.removeAll(toRemove);

            // Update passengers with new ones
            passengers.addAll(newPassengers);

            // Handle current non-player passengers
            for (LivingEntity e : passengers) {
                if (!(e instanceof Player)) {
                    // Always teleport non-players

                    e.setFallDistance(0);
                    Location entityLoc = e.getLocation();
                    double teleportY = y + 1;

                    if (direction > 0)
                        teleportY += 0.15;
                    else
                        teleportY -= 0.15;

                    entityLoc.setY(teleportY);
                    if (e.getVehicle() != null)
                        e.getVehicle().teleport(entityLoc);
                    else
                        e.teleport(entityLoc);
                }
            }
        }
    }

    public void end() {
        if (isBaseBlock()) {
            for (LivingEntity e : getEntitiesInsideBlock()) {
                Location entityLoc = e.getLocation();
                double teleportY = getCurrentLocation().getBlockY() + 1.01;
                entityLoc.setY(teleportY);
                if (e.getVehicle() != null)
                    e.getVehicle().teleport(entityLoc);
                else
                    e.teleport(entityLoc);
            }

            // Deal with passengers who are getting off the elevator
            for (LivingEntity e : passengers) {
                parent.unfreezePassengerMovement(e);
            }
        }

        getCurrentLocation().toBlockLocation().getBlock().setBlockData(blockData, false);

        if (blockEntity() != null) {
            blockEntity().remove();
        }

        atDestination = false;
    }

    public int getCurrentY() {
        return getCurrentLocation().getBlockY() - baseDifference;
    }

    public boolean isValid() {
        return blockEntity() != null && blockEntity().isValid();
    }

    public boolean isBaseBlock() {
        return baseDifference == 0;
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

    /**
     * Unlike getEntitiesOnBlock, this only returns entities that were last in the elevator when it was moving
     */
    public HashSet<LivingEntity> getLastKnownPassengers() {
        return passengers;
    }

}
