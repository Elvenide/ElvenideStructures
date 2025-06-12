package com.elvenide.structures.elevators;

import com.elvenide.core.Core;
import com.elvenide.core.providers.config.ConfigSection;
import com.elvenide.structures.ElvenideStructures;
import com.elvenide.structures.Structure;
import com.elvenide.structures.doors.Door;
import com.elvenide.structures.elevators.events.ElevatorEndEvent;
import com.elvenide.structures.elevators.events.ElevatorStartEvent;
import com.elvenide.structures.selection.Selection;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class Elevator implements Structure {

    private final ConfigSection config;
    private final HashSet<ElevatorBlock> elevatorBlocks = new HashSet<>();
    private boolean isMoving = false;
    private long cooldown = 0;
    Player moveDelayTrigger = null;

    public Elevator(ConfigSection config) {
        this.config = config;
    }

    /// Get the block locations of this elevator's carriage
    public List<Location> getCarriageBlocks() {
        ArrayList<Location> blocks = new ArrayList<>();

        if (!config.contains("locations"))
            return blocks;

        for (String key : config.getSection("locations").getKeys()) {
            blocks.add(config.getSection("locations").getLocation(key));
        }
        return blocks;
    }

    /// Gets the block locations that are on the base-level
    private HashSet<Location> getBaseBlocks() {
        HashSet<Location> baseBlocks = new HashSet<>();

        // Get all base-level blocks
        for (Location loc : getCarriageBlocks()) {
            if (loc.getBlockY() == getBaseY())
                baseBlocks.add(loc);
        }

        return baseBlocks;
    }

    /// Gets the block locations that are adjacent to the entrance(s) of this elevator on the base-level
    public HashSet<Location> getEntryAdjacentBlocks() {
        HashSet<Location> groundBaseBlocks = new HashSet<>();
        HashSet<Location> allBlocks = new HashSet<>(getCarriageBlocks());
        HashSet<Location> borderBlocks = new HashSet<>();

        // Get all base-level blocks that can be stood on
        for (Location loc : getBaseBlocks()) {
            if (isWalkable(loc.getBlock().getRelative(BlockFace.UP))
                && isWalkable(loc.getBlock().getRelative(BlockFace.UP, 2)))
                groundBaseBlocks.add(loc);
        }

        // Get blocks that border entry base-level blocks
        for (Location loc : groundBaseBlocks) {
            boolean isNorthBorder = !allBlocks.contains(loc.getBlock().getRelative(BlockFace.NORTH).getLocation());
            boolean isSouthBorder = !allBlocks.contains(loc.getBlock().getRelative(BlockFace.SOUTH).getLocation());
            boolean isEastBorder = !allBlocks.contains(loc.getBlock().getRelative(BlockFace.EAST).getLocation());
            boolean isWestBorder = !allBlocks.contains(loc.getBlock().getRelative(BlockFace.WEST).getLocation());

            if (isNorthBorder)
                borderBlocks.add(loc.getBlock().getRelative(BlockFace.NORTH).getLocation());
            if (isSouthBorder)
                borderBlocks.add(loc.getBlock().getRelative(BlockFace.SOUTH).getLocation());
            if (isEastBorder)
                borderBlocks.add(loc.getBlock().getRelative(BlockFace.EAST).getLocation());
            if (isWestBorder)
                borderBlocks.add(loc.getBlock().getRelative(BlockFace.WEST).getLocation());
        };

        return borderBlocks;
    }

    /// Get the block locations that are adjacent to the entrance(s) of this elevator on the current level
    public HashSet<Location> getCurrentEntryAdjacentBlocks() {
        return new HashSet<>(
            getEntryAdjacentBlocks().stream()
                .map(loc -> {
                    Location current = loc.clone();
                    current.setY(getCurrentY());
                    return current;
                }).toList()
        );
    }

    /// Get a door that is connected to this elevator, if any
    public @Nullable Door getConnectedDoor() {
        return getCurrentEntryAdjacentBlocks().stream()
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
        return getElevatorBlocks().stream().filter(ElevatorBlock::isBaseBlock).map(ElevatorBlock::getEntitiesOnBlock).flatMap(List::stream).collect(Collectors.toSet());
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

    /// Set the current floor Y level of this elevator
    public void setCurrentY(int y) {
        config.set("current-level", y);
        config.getRoot().save();
    }

    /// If a block is not passable
    private boolean isSolid(Block block) {
        // Do not consider barriers to be solid
        if (block.getType() == Material.BARRIER)
            return false;

        // Do not consider custom door blocks to be solid
        if (ElvenideStructures.doors().isDoorBlock(block.getLocation()))
            return false;

        return !block.isPassable();
    }

    /// If a block is either passable or can be opened to pass through
    private boolean isWalkable(Block block) {
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

    /// Find a 2-block passable opening in the given direction
    private @Nullable Location findOpeningInDirection(Location loc, BlockFace direction) {
        HashSet<Location> baseBlocks = getBaseBlocks();
        HashSet<Location> allBlocks = new HashSet<>(getCarriageBlocks());

        // Calculate the maximum distance in this direction that the elevator can travel
        int maxY;
        outer:
        for (maxY = 1; maxY <= 50; maxY++) {
            for (Location baseBlock : baseBlocks) {
                Block projectedBaseBlock = baseBlock.getBlock().getRelative(direction, maxY);
                if (!projectedBaseBlock.getType().isAir() && !allBlocks.contains(projectedBaseBlock.getLocation())) {
                    maxY--;
                    break outer;
                }
            }
        }

        // Find the closest opening in this direction, if any can be found where the elevator can travel
        for (int i = 1; i <= maxY; i++) {
            Block above = loc.getBlock().getRelative(direction, i);
            if (isSolid(above) && isWalkable(above.getRelative(BlockFace.UP))
                && isWalkable(above.getRelative(BlockFace.UP, 2)))
                return above.getLocation();
        }

        return null;
    }

    /// Get the destination floor Y level of this elevator (or the base floor if already at destination)
    public int getDestinationY() throws IllegalStateException {
        if (getCurrentY() != getBaseY())
            return getBaseY();

        HashSet<Location> entryAdjacents = getEntryAdjacentBlocks();

        for (Location loc : entryAdjacents) {
            Location openingAbove = findOpeningInDirection(loc, BlockFace.UP);
            Location openingBelow = findOpeningInDirection(loc, BlockFace.DOWN);

            if (openingAbove != null && openingBelow == null)
                return openingAbove.getBlockY();

            if (openingBelow != null && openingAbove == null)
                return openingBelow.getBlockY();

            if (openingAbove != null)
                return Math.min(openingAbove.getBlockY(), openingBelow.getBlockY());
        }

        throw new IllegalStateException("No valid destination Y level found.");
    }

    /// Get the speed at which blocks move in this elevator
    public double getSpeed() {
        return config.getDouble("speed");
    }

    /// Get the delay (in secs) that a player must stand in the elevator before it starts moving
    public double getMoveDelay() {
        return 5;
    }

    /// Get the cooldown (in secs) before anyone can re-run this elevator
    public double getReuseCooldown() {
        int destinationY;
        try {
            destinationY = getDestinationY();
        } catch (IllegalStateException e) {
            // Raw 1s cooldown if no destination
            return 1;
        }

        // Base cooldown
        double base = ElvenideStructures.elevators().getConfiguredCooldownSecs(getCarriageBlocks().getFirst().getWorld());

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

    /// Sets the blocks of this elevator's carriage
    public void setCarriageBlocks(Selection carriage) throws IllegalStateException {
        int baseY = 100000;
        if (!config.contains("locations"))
            config.createSection("locations");
        ConfigSection locations = config.getSection("locations");
        assert locations != null;

        for (Location loc : carriage) {
            if (isSolid(loc.getBlock()) && isWalkable(loc.getBlock().getRelative(BlockFace.UP))
                && isWalkable(loc.getBlock().getRelative(BlockFace.UP, 2))) {
                baseY = Math.min(baseY, loc.getBlockY());
            }

            locations.set(loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ(), loc);
        }

        if (baseY == 100000)
            throw new IllegalStateException("No valid base Y level found in selection.");

        config.set("base-level", baseY);
        config.getRoot().save();
    }

    /// Check if a location is within range of the floors of this elevator
    @Override
    public boolean isNearby(Location loc) {
        int currentY = getCurrentY();
        int destinationY;
        try {
            destinationY = getDestinationY();
        } catch (IllegalStateException e) {
            return false;
        }

        double range = ElvenideStructures.elevators().getConfiguredConnectionRange(loc.getWorld());
        if (Math.abs(currentY - loc.getBlockY()) > range && Math.abs(destinationY - loc.getBlockY()) > range)
            return false;

        for (Location baseBlock : getBaseBlocks()) {
            Location adjustedBaseBlock = baseBlock.clone();
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

    /// Gets the ElevatorBlock instances for this elevator; if none exist, creates them
    public HashSet<ElevatorBlock> getElevatorBlocks() {
        HashSet<Location> carriageLocations = new HashSet<>(getCarriageBlocks());
        if (elevatorBlocks.isEmpty())
            elevatorBlocks.addAll(carriageLocations.stream().map(b -> new ElevatorBlock(b, this)).toList());

        return elevatorBlocks;
    }

    /// Moves the elevator carriage to the floor nearest to the given location (if possible, or errors otherwise)
    public void moveToNearestFloor(Location nearbyLoc) throws IllegalStateException {
        int destinationY;
        try {
            destinationY = getDestinationY();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("<red>✖ Failed to move elevator to nearest floor: Could not find a valid destination floor that would connect to the elevator's entrances.");
        }

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

    /// Moves the elevator carriage from one floor to another
    public void move(boolean ignoreCooldown) throws IllegalStateException {
        if (isMoving) {
            throw new IllegalStateException("<red>✖ The elevator is already moving.");
        }

        if (!ignoreCooldown && isOnCooldown())
            throw new IllegalStateException(Core.text.format("<red>✖ You must wait %.1f seconds before re-running this elevator.", getReuseCooldown()));

        int targetY;

        try {
            targetY = getDestinationY();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("<red>✖ Failed to move elevator: Could not find a valid destination floor that would connect to the elevator's entrances.");
        }

        if (new ElevatorStartEvent(this).callCoreEvent().isCancelled())
            return;

        isMoving = true;
        cooldown = System.currentTimeMillis();

        int currentY = getCurrentY();
        int direction = (targetY > currentY ? 1 : -1);

        for (ElevatorBlock block : getElevatorBlocks())
            block.spawn(targetY);

        Core.tasks.builder()
            .then(task -> {
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

    public void freezePassengerMovement(LivingEntity e) {
        if (e instanceof Player p) {
            p.setAllowFlight(true);
            p.setFlySpeed(0);
            p.setWalkSpeed(0);
            p.setFlying(true);
        }
        else e.setGravity(false);
    }

    public void unfreezePassengerMovement(LivingEntity e) {
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
