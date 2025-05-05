package com.elvenide.structures.elevators;

import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ElevatorBlock {

    private BlockData blockData;
    private BlockDisplay display;
    private final Location currentLocation;
    private final Elevator parent;
    private final HashSet<LivingEntity> previousPassengers = new HashSet<>();
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

    public void spawn(int targetY) {
        if (isBaseBlock())
            for (LivingEntity e : getEntitiesOnBlock()) {
                if (e instanceof Player p) {
                    p.setAllowFlight(true);
                    p.setFlying(false);
                }
                e.setGravity(false);

                previousPassengers.add(e);
            }

        this.blockData = getCurrentLocation().getBlock().getBlockData();
        getCurrentLocation().toBlockLocation().getBlock().setBlockData(Material.AIR.createBlockData(), false);
        this.display = getCurrentLocation().getWorld().spawn(getCurrentLocation(), BlockDisplay.class, b -> {
            b.setBlock(blockData);
            b.setInterpolationDelay(0);
            b.setTeleportDuration(1);
        });

        atDestination = false;
        this.targetY = targetY + baseDifference;
    }

    public void move(double direction) {
        double blocksPerTick = parent.getSpeed() * direction / 20.0;

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
        display.teleport(getCurrentLocation());

        // Move any entities on elevator
        if (isBaseBlock()) {
            HashSet<LivingEntity> currentPassengers = new HashSet<>(getEntitiesOnBlock());
            for (LivingEntity e : previousPassengers) {
                if (currentPassengers.contains(e))
                    continue;

                if (e instanceof Player p) {
                    p.setFlying(false);
                    if (p.getGameMode() != GameMode.CREATIVE)
                        p.setAllowFlight(false);
                }

                e.setGravity(true);
            }
            previousPassengers.clear();

            for (LivingEntity e : currentPassengers) {
                e.setGravity(false);
                if (e instanceof Player p) {
                    p.setAllowFlight(true);
                    p.setFlying(false);
                }
                if (atDestination && e instanceof Player p) {
                    p.setFlying(false);
                    if (p.getGameMode() != GameMode.CREATIVE)
                        p.setAllowFlight(false);
                }
                if (atDestination)
                    e.setGravity(true);

                e.setFallDistance(0);
                previousPassengers.add(e);

                if (e instanceof Player) {
                    // If too far below expected location, teleport player
                    if (e.getLocation().getY() < getCurrentLocation().getY() + 0.8) {
                        Location entityLoc = e.getLocation().clone();
                        double teleportY = y + 1 - 0.1;
                        entityLoc.setY(teleportY);
                        e.teleport(entityLoc, TeleportFlag.EntityState.RETAIN_PASSENGERS, TeleportFlag.EntityState.RETAIN_VEHICLE);
                        e.setVelocity(new Vector(0, 0, 0));
                        continue;
                    }

                    // Otherwise, move player using smooth velocity
                    e.setVelocity(new Vector(0, 0, 0));
                    e.setVelocity(new Vector(0, blocksPerTick, 0));
                }
                else {
                    // Always teleport non-players
                    Location entityLoc = e.getLocation();
                    double teleportY = y + 1;

                    if (direction > 0)
                        teleportY += 0.15;
                    else
                        teleportY -= 0.15;

                    entityLoc.setY(teleportY);
                    e.teleport(entityLoc, TeleportFlag.EntityState.RETAIN_PASSENGERS, TeleportFlag.EntityState.RETAIN_VEHICLE);
                }
            }
        }
    }

    public void end() {
        if (isBaseBlock())
            for (LivingEntity e : getEntitiesInsideBlock()) {
                Location entityLoc = e.getLocation();
                double teleportY = getCurrentLocation().getBlockY() + 1.01;
                entityLoc.setY(teleportY);
                e.teleport(entityLoc, TeleportFlag.EntityState.RETAIN_PASSENGERS, TeleportFlag.EntityState.RETAIN_VEHICLE);
            }

        getCurrentLocation().toBlockLocation().getBlock().setBlockData(blockData, false);

        if (display != null) {
            display.remove();
        }

        atDestination = false;
    }

    public int getCurrentY() {
        return getCurrentLocation().getBlockY() - baseDifference;
    }

    public boolean isValid() {
        return display != null && display.isValid();
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

}
