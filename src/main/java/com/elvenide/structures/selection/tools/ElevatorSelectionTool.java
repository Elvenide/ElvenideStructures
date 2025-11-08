package com.elvenide.structures.selection.tools;

import com.elvenide.core.Core;
import com.elvenide.core.providers.item.ItemBuilder;
import com.elvenide.structures.selection.Selection;
import com.elvenide.structures.selection.SelectionTool;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ElevatorSelectionTool extends SelectionTool {

    private @Nullable Location destFloor = null;

    public ElevatorSelectionTool(Player user) {
        super(user);
    }

    @Override
    protected ItemBuilder create(ItemBuilder builder) {
        return builder.lore("<yellow>Shift-right-click <gray>to select destination floor.");
    }

    @Override
    public @Nullable Selection getSelection() {
        Selection s = super.getSelection();
        if (s != null)
            s.setTertiaryPosition(destFloor);

        return s;
    }

    @Override
    protected @Nullable String getCompletionError() {
        if (destFloor == null)
            return "<red>Cannot complete selection; destination floor must be set.<br><red>Use <yellow>shift-right-click</yellow> to set it.";

        return super.getCompletionError();
    }

    @Override
    protected void onShiftRightClick(Block block, Player player) {
        destFloor = block.getLocation();
        Selection s = getSelection();

        if (s == null)
            Core.text.send(player, "<aqua>Base floor Y level will be set to the lowest point within the two corners you select.");
        else
            Core.text.send(player, "<aqua>Base floor Y level set to <dark_aqua>{}", s.getMinimumY());
        Core.text.send(player, "<aqua>Destination floor Y level set to <dark_aqua>{}", destFloor.getBlockY());
    }
}
