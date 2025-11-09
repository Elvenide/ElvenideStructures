package com.elvenide.structures.elevators;

import com.elvenide.core.Core;
import com.elvenide.core.providers.config.ConfigSection;
import com.elvenide.structures.ElvenideStructures;
import com.elvenide.structures.Structure;
import com.elvenide.structures.doors.Door;
import com.elvenide.structures.elevators.events.ElevatorEndEvent;
import com.elvenide.structures.elevators.events.ElevatorStartEvent;
import com.elvenide.structures.selection.Selection;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class Elevator implements Structure {

    private final ConfigSection config;
    private final ArrayList<Location> carriageLocations = new ArrayList<>();
    private final HashSet<ElevatorBlock> elevatorBlocks = new HashSet<>();
    private boolean isMoving = false;
    private long cooldown = 0;
    Player moveDelayTrigger = null;

    public Elevator(ConfigSection config) {
        this.config = config;
    }


    /* <editor-fold defaultstate="collapsed" desc="Getters"> */

    /// Get the block locations of this elevator's carriage
    public List<Location> getCarriageBlockLocations() {
        if (!carriageLocations.isEmpty())
            return carriageLocations;

        if (!config.contains("locations"))
            return carriageLocations;

        for (String key : config.getSection("locations").getKeys()) {
            carriageLocations.add(config.getSection("locations").getLocation(key));
        }
        return carriageLocations;
    }

    /// Gets the carriage block locations that can be stood on by humanoid entities
    public HashSet<Location> getFloorBlockLocations() {
        HashSet<Location> blocks = new HashSet<>();

        for (Location loc : getCarriageBlockLocations()) {
            if (ElevatorUtils.isSolid(loc.getBlock()) && ElevatorUtils.isWalkable(loc.getBlock().getRelative(BlockFace.UP))
                && ElevatorUtils.isWalkable(loc.getBlock().getRelative(BlockFace.UP, 2)))
                blocks.add(loc);
        }

        return blocks;
    }

    /// Gets the ElevatorBlock instances for this elevator; if none exist, creates them
    public HashSet<ElevatorBlock> getElevatorBlocks() {
        if (!elevatorBlocks.isEmpty())
            return elevatorBlocks;

        HashSet<Location> carriageLocations = new HashSet<>(getCarriageBlockLocations());
        elevatorBlocks.addAll(carriageLocations.stream().map(b -> new ElevatorBlock(b, this, getFloorBlockLocations().contains(b))).toList());
        return elevatorBlocks;
    }

    /// Get a door that is connected to this elevator, if any
    public @Nullable Door getConnectedDoor() {
        return getFloorBlockLocations().stream()
            .map(loc -> {
                Location newLoc = loc.clone();
                newLoc.setY(getCurrentY());
                return newLoc;
            })
            .map(loc -> ElvenideStructures.doors().getNearby(loc))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    /// Get all passengers known to be riding this elevator when it was last moving
    public Set<LivingEntity> getLastKnownPassengers() {
        return getElevatorBlocks().stream().map(ElevatorBlock::getLastKnownPassengers).flatMap(HashSet::stream).collect(Collectors.toSet());
    }

    /// Get all passengers currently standing inside this elevator
    public Set<LivingEntity> getCurrentlyInside() {
        return getElevatorBlocks().stream().filter(ElevatorBlock::isFloorBlock).map(ElevatorBlock::getEntitiesOnBlock).flatMap(List::stream).collect(Collectors.toSet());
    }

    /// Get the number of blocks in this elevator's carriage
    public int getCarriageSize() {
        if (!config.contains("locations"))
            return 0;
        return config.getSection("locations").getKeys().size();
    }

    /// Get the base floor Y level of this elevator
    public int getBaseY() {
        return config.getInt("base-level");
    }

    /// Get the current floor Y level of this elevator
    public int getCurrentY() {
        return config.getInt("current-level", getBaseY());
    }

    /// Get the destination floor Y level of this elevator (or the base floor if already at destination)
    public int getDestinationY() {
        if (getCurrentY() != getBaseY())
            return getBaseY();

        return config.getInt("dest-level");
    }

    /// Get the speed at which blocks move in this elevator
    public double getSpeed() {
        return config.getDouble("speed", 1);
    }

    /// Get the delay (in secs) that a player must stand in the elevator before it starts moving
    public double getMoveDelay() {
        return config.getDouble("walk-in-delay-secs", 5);
    }

    /// Get the cooldown (in secs) before anyone can re-run this elevator
    public double getReuseCooldown() {
        int destinationY = getDestinationY();

        // Base cooldown
        double base = ElvenideStructures.elevators().getConfiguredCooldownSecs(getCarriageBlockLocations().getFirst().getWorld());

        // Base cooldown + elevator travel time for cooldown
        return base + Math.abs(getCurrentY() - destinationY)/getSpeed();
    }

    /// Get the remaining secs before the reuse cooldown ends
    public double getRemainingCooldown() {
        double total = getReuseCooldown();
        double now = System.currentTimeMillis();
        return Math.max(total - (now - cooldown) / 1000D, 0.1);
    }

    /// Get the name of this elevator
    public String getName() {
        return config.getName();
    }

    /* </editor-fold> */


    /* <editor-fold defaultstate="collapsed" desc="Setters"> */

    /// Set the current floor Y level of this elevator
    public void setCurrentY(int y) {
        config.set("current-level", y);
        config.getRoot().save();
    }

    /// Sets the blocks of this elevator's carriage
    public void setCarriageBlocks(Selection carriage) {
        if (!config.contains("locations"))
            config.createSection("locations");
        ConfigSection locations = config.getSection("locations");
        assert locations != null;

        int minY = carriage.getMinimumY();
        for (Location loc : carriage)
            locations.set(loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ(), loc);

        config.set("base-level", minY);
        config.set("dest-level", carriage.getTertiaryPosition());
        config.getRoot().save();
    }

    /* </editor-fold> */


    /* <editor-fold defaultstate="collapsed" desc="Boolean Flags"> */

    /// Check if a location is within range of the floors of this elevator
    @Override
    public boolean isNearby(Location loc) {
        int currentY = getCurrentY();
        int destinationY = getDestinationY();

        double range = ElvenideStructures.elevators().getConfiguredConnectionRange(loc.getWorld());
        if (Math.abs(currentY - loc.getBlockY()) > range && Math.abs(destinationY - loc.getBlockY()) > range)
            return false;

        for (Location floorBlock : getFloorBlockLocations()) {
            Location adjustedBaseBlock = floorBlock.clone();
            adjustedBaseBlock.setY(loc.getY());
            if (loc.distance(adjustedBaseBlock) <= range + 0.5)
                return true;
        }

        return false;
    }

    /// Check if a player is standing inside this elevator
    public boolean isInside(Player player) {
        return getCurrentlyInside().contains(player);
    }

    /// Check if this elevator is currently moving
    public boolean isMoving() {
        return isMoving;
    }

    /// Check if this elevator is on cooldown
    public boolean isOnCooldown() {
        long now = System.currentTimeMillis();
        return now - cooldown < getReuseCooldown() * 1000;
    }

    /* </editor-fold> */


    /* <editor-fold defaultstate="collapsed" desc="Movement"> */

    /// Moves the elevator carriage to the floor nearest to the given location (if possible, or errors otherwise)
    public void moveToNearestFloor(Location nearbyLoc) throws IllegalStateException {
        int destinationY = getDestinationY();

        int currentDist = Math.abs(nearbyLoc.getBlockY() - getCurrentY());
        int otherDist = Math.abs(nearbyLoc.getBlockY() - destinationY);

        if (currentDist > otherDist)
            move();
        else
            throw new IllegalStateException("<red>✖ The elevator is already on this floor.");
    }

    /// Moves the elevator carriage from one floor to another, if not on cooldown
    public void move() throws IllegalStateException {
        move(false);
    }

    /// Moves the elevator carriage from one floor to another, ignoring cooldown
    public void moveWithoutCooldown() throws IllegalStateException {
        move(true);
    }

    /// Moves the elevator carriage from one floor to another
    private void move(boolean ignoreCooldown) throws IllegalStateException {
        if (isMoving) {
            throw new IllegalStateException("<red>✖ The elevator is already moving.");
        }

        if (!ignoreCooldown && isOnCooldown())
            throw new IllegalStateException(Core.text.format("<red>✖ You must wait %.1f seconds before re-running this elevator.", getRemainingCooldown()));

        int targetY = getDestinationY();

        if (new ElevatorStartEvent(this).callCoreEvent().isCancelled())
            return;

        isMoving = true;
        cooldown = System.currentTimeMillis();

        int currentY = getCurrentY();
        int direction = (targetY > currentY ? 1 : -1);

        for (ElevatorBlock block : getElevatorBlocks())
            block.spawn(targetY);

        Core.tasks.create(task -> {
            ElevatorBlock elevatorBlock = getElevatorBlocks().iterator().next();
            if (elevatorBlock.atDestination || !elevatorBlock.isValid()) {
                getElevatorBlocks().forEach(ElevatorBlock::end);
                setCurrentY(elevatorBlock.getCurrentY());
                isMoving = false;
                task.cancel();
                new ElevatorEndEvent(this).callCoreEvent();
                return;
            }

            getElevatorBlocks().forEach(block -> block.move(direction));

            // Handle player passengers
            double blocksPerTick = elevatorBlock.getBlocksPerTick(direction);
            getLastKnownPassengers().forEach(e -> {
                if (e instanceof Player p) {
                    p.setFallDistance(0);
                    p.setVelocity(new Vector(0, blocksPerTick, 0));

                    // Re-enable flying every tick to prevent the player from manually disabling it
                    p.setFlying(true);
                }
            });
        })
        .repeat(0L, 1L);
    }

    /* </editor-fold> */


    @Override
    public void onNearbySwitchUsed(Player user, Location loc) {
        try {
            moveToNearestFloor(loc.clone());
        } catch (IllegalStateException e) {
            Core.text.send(user, e.getMessage());
            return;
        }

        Core.text.send(user, "<smooth_blue>ℹ You've called the elevator, it will arrive soon...");
    }

}
