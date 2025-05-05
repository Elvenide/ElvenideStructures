package com.elvenide.structures.selection;

import com.elvenide.core.ElvenideCore;
import com.elvenide.structures.selection.events.SelectionEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class SelectionManager implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND)
            return;

        if (!event.getAction().isLeftClick() && !event.getAction().isRightClick())
            return;

        if (!event.hasItem())
            return;

        if (!SelectionTool.is(event.getItem()))
            return;

        event.setCancelled(true);

        if (event.getClickedBlock() != null && event.getAction().isLeftClick() && !event.getPlayer().isSneaking()) {
            Location l = event.getClickedBlock().getLocation();
            if (SelectionTool.selectCorner1(event.getItem(), l))
                ElvenideCore.text.send(event.getPlayer(), "<green>Corner #1 set to <dark_green>%d, %d, %d", l.getBlockX(), l.getBlockY(), l.getBlockZ());
            else return;
        }
        else if (event.getClickedBlock() != null && event.getAction().isRightClick() && !event.getPlayer().isSneaking()) {
            Location l = event.getClickedBlock().getLocation();
            if (SelectionTool.selectCorner2(event.getItem(), l))
                ElvenideCore.text.send(event.getPlayer(), "<light_purple>Corner #2 set to <dark_purple>%d, %d, %d", l.getBlockX(), l.getBlockY(), l.getBlockZ());
            else return;
        }

        Selection selection = SelectionTool.getSelection(event.getItem(), event.getPlayer().getWorld());
        if (selection == null) {
            if (event.getPlayer().isSneaking())
                ElvenideCore.text.send(event.getPlayer(), "<red>Cannot complete selection; both corners must be selected.");
            return;
        }

        if (event.getPlayer().isSneaking()) {
            new SelectionEvent(selection, event.getPlayer()).callEvent();
            Objects.requireNonNull(SelectionVisualizer.getExisting(event.getPlayer())).remove();
        }
        else
            Objects.requireNonNull(SelectionVisualizer.getNew(selection, event.getPlayer())).create();
    }

    @EventHandler
    public void onSwitchItem(PlayerItemHeldEvent event) {
        ItemStack prevItem = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
        ItemStack currentItem = event.getPlayer().getInventory().getItem(event.getNewSlot());

        SelectionVisualizer visualizer = SelectionVisualizer.getExisting(event.getPlayer());

        if (visualizer != null && prevItem != null && SelectionTool.is(prevItem)) {
            visualizer.remove();
        }
        else if (currentItem != null && SelectionTool.is(currentItem)) {
            if (visualizer == null)
                visualizer = SelectionVisualizer.getNew(SelectionTool.getSelection(currentItem, event.getPlayer().getWorld()), event.getPlayer());

            if (visualizer != null)
                visualizer.create();
        }
    }

}
