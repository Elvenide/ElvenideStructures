package com.elvenide.structures.doors.types;

import com.elvenide.core.providers.config.ConfigSection;
import com.elvenide.structures.ElvenideStructures;
import com.elvenide.structures.doors.Door;
import com.elvenide.structures.doors.DoorBlock;
import com.elvenide.structures.selection.Selection;
import com.elvenide.structures.selection.tools.SlidingDoorSelectionTool;
import org.bukkit.Location;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class SlidingDoor implements Door {

    private final ConfigSection config;
    private final List<DoorBlock> blocks;
    private boolean moving = false;

    public SlidingDoor(ConfigSection config) {
        this.config = config;
        this.blocks = getLocations().stream().map(l -> Door.createBlock(l, l.getBlock().getBlockData())).toList();
    }

    private List<Location> getLocations() {
        ArrayList<Location> blocks = new ArrayList<>();

        if (!config.contains("locations"))
            return blocks;

        for (String key : config.getSection("locations").getKeys()) {
            blocks.add(config.getSection("locations").getLocation(key));
        }
        return blocks;
    }

    @Override
    public int getMoveDuration() {
        return (int) (config.getFloat("move-duration") * 20);
    }

    public Vector getMoveDirection() {
        return config.getVector("move-direction");
    }

    public int getMoveDistance() {
        return config.getInt("move-distance");
    }

    @Override
    public List<DoorBlock> getBlocks() {
        return blocks;
    }

    @Override
    public Transformation getTransformationAtPercent(DoorBlock block, double percent) {
        Transformation transformation = block.display().getTransformation();

        Vector direction = getMoveDirection().clone();
        int distance = getMoveDistance();
        direction.multiply(percent * distance);

        transformation.getTranslation().set((float) direction.getX(), (float) direction.getY(), (float) direction.getZ());
        return transformation;
    }

    @Override
    public boolean isOpen() {
        return config.getBoolean("open", false);
    }

    @Override
    public void setOpen(boolean open) {
        config.setAndSave("open", open);
    }

    @Override
    public boolean isMoving() {
        return moving;
    }

    @Override
    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public static Door create(String name, float moveDuration, Selection selection, SlidingDoorSelectionTool tool) {
        ConfigSection doors = ElvenideStructures.doors().getStructures(selection.getWorld()).getSection("doors");
        if (doors == null)
            doors = ElvenideStructures.doors().getStructures(selection.getWorld()).createSection("doors");

        ConfigSection section = doors.createSection(name);
        section.set("move-duration", moveDuration);
        section.set("move-direction", tool.getMoveDirection());
        section.set("move-distance", tool.getMoveDistance());
        section.set("open", false);
        section.set("type", "sliding");

        ConfigSection locations = section.createSection("locations");
        for (Location loc : selection) {
            locations.set(loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ(), loc);
        }
        section.getRoot().save();

        Door door = new SlidingDoor(section);
        ElvenideStructures.doors().add(selection.getWorld(), name, door);
        return door;
    }
}
