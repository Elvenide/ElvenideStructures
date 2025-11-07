package com.elvenide.structures.selection;

import com.elvenide.core.Core;
import com.elvenide.core.api.PublicAPI;
import com.elvenide.core.providers.item.ItemBuilder;
import com.elvenide.structures.ElvenideStructures;
import com.elvenide.structures.Keys;
import com.elvenide.structures.selection.events.SelectionCancelEvent;
import com.elvenide.structures.selection.events.SelectionEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public abstract class SelectionTool implements Listener {

    private final UUID user;
    private final UUID uuid;
    private final ItemStack item;
    protected Location corner1 = null;
    protected Location corner2 = null;

    @PublicAPI
    public SelectionTool(Player user) {
        this.user = user.getUniqueId();
        this.uuid = UUID.randomUUID();
        ItemBuilder builder = create(Core.items.create(Material.NETHERITE_AXE)
            .name("<smooth_purple>Elvenide's Selection Tool")
            .data(Keys.SELECTION_TOOL, PersistentDataType.STRING, uuid.toString())
            .lore("<yellow>Left-click <gray>to select corner #1")
            .lore("<yellow>Right-click <gray>to select corner #2"));
        builder.lore("<yellow>Press F <gray>to complete selection");
        this.item = builder.build();

        Core.plugin.registerListeners(this);
    }

    protected abstract ItemBuilder create(ItemBuilder builder);

    @PublicAPI
    public ItemStack getItem() {
        return item;
    }

    @PublicAPI
    public boolean is(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return false;
        return Core.items.getData(item, Keys.SELECTION_TOOL, PersistentDataType.STRING, "").equals(uuid.toString());
    }

    /// Returns false if the corner is already selected
    @PublicAPI
    public boolean selectCorner1(Location corner1) {
        if (this.corner1 != null && this.corner1.equals(corner1))
            return false;

        this.corner1 = corner1;
        return true;
    }

    /// Returns false if the corner is already selected
    @PublicAPI
    public boolean selectCorner2(Location corner2) {
        if (this.corner2 != null && this.corner2.equals(corner2))
            return false;

        this.corner2 = corner2;
        return true;
    }

    @PublicAPI
    public @Nullable Selection getSelection() {
        if (corner1 == null || corner2 == null)
            return null;

        return new Selection(corner1, corner2);
    }

    /// Returns an error message if there are any selection errors
    protected @Nullable String getCompletionError() {
        if (corner1 == null)
            return "<red>Cannot complete selection; corner #1 must be selected.";

        if (corner2 == null)
            return "<red>Cannot complete selection; corner #2 must be selected.";

        return null;
    }

    /// Called when the player left-clicks a block
    protected void onLeftClick(Block block, Player player) {
        Location l = block.getLocation();
        if (!selectCorner1(l))
            return;

        Core.text.send(player, "<green>Corner #1 set to <dark_green>{}, {}, {}", l.getBlockX(), l.getBlockY(), l.getBlockZ());

        Selection selection = getSelection();
        if (selection != null)
            Objects.requireNonNull(SelectionVisualizer.getNew(selection, player)).create();
    }

    /// Called when the player right-clicks a block
    protected void onRightClick(Block block, Player player) {
        Location l = block.getLocation();
        if (!selectCorner2(l))
            return;

        Core.text.send(player, "<light_purple>Corner #2 set to <dark_purple>{}, {}, {}", l.getBlockX(), l.getBlockY(), l.getBlockZ());

        Selection selection = getSelection();
        if (selection != null)
            Objects.requireNonNull(SelectionVisualizer.getNew(selection, player)).create();
    }

    /// Called when the player shift-left-clicks a block
    protected void onShiftLeftClick(Block block, Player player) {
        // Does nothing by default
    }

    /// Called when the player shift-left-clicks, whether a block or not
    protected void onShiftLeftClick(Player player) {
        // Does nothing by default
    }

    /// Called when the player shift-right-clicks a block
    protected void onShiftRightClick(Block block, Player player) {
        // Does nothing by default
    }

    /// Called when the player shift-right-clicks, whether a block or not
    protected void onShiftRightClick(Player player) {
        // Does nothing by default
    }

    /// Called when the player presses F (offhand swap)
    protected void onFPress(Player player) {
        Selection selection = getSelection();
        @Nullable String error = getCompletionError();
        if (selection == null || error != null) {
            Core.text.send(player, error);
            return;
        }

        new SelectionEvent(selection, player).callCoreEvent();
        Objects.requireNonNull(SelectionVisualizer.getExisting(player)).remove();
        Core.plugin.unregisterListeners(this);
    }

    @EventHandler
    public final void onInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND)
            return;

        if (!event.getAction().isLeftClick() && !event.getAction().isRightClick())
            return;

        if (!event.hasItem())
            return;

        if (!is(event.getItem()))
            return;

        event.setCancelled(true);

        if (event.getAction().isLeftClick()) {
            if (event.getPlayer().isSneaking()) {
                if (event.getClickedBlock() != null)
                    onShiftLeftClick(event.getClickedBlock(), event.getPlayer());
                onShiftLeftClick(event.getPlayer());
            }
            else if (event.getClickedBlock() != null)
                onLeftClick(event.getClickedBlock(), event.getPlayer());
        }
        else if (event.getAction().isRightClick()) {
            if (event.getPlayer().isSneaking()) {
                if (event.getClickedBlock() != null)
                    onShiftRightClick(event.getClickedBlock(), event.getPlayer());
                onShiftRightClick(event.getPlayer());
            }
            else if (event.getClickedBlock() != null)
                onRightClick(event.getClickedBlock(), event.getPlayer());
        }
    }

    @EventHandler
    public final void onOffhandSwap(PlayerSwapHandItemsEvent event) {
        if (!is(event.getOffHandItem()))
            return;

        event.setCancelled(true);
        onFPress(event.getPlayer());
    }

    @EventHandler
    public final void onSwitchItem(PlayerItemHeldEvent event) {
        ItemStack prevItem = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
        ItemStack currentItem = event.getPlayer().getInventory().getItem(event.getNewSlot());

        SelectionVisualizer visualizer = SelectionVisualizer.getExisting(event.getPlayer());

        if (visualizer != null && prevItem != null && is(prevItem)) {
            visualizer.remove();
        }
        else if (currentItem != null && is(currentItem)) {
            // Get non-null selection
            Selection selection = getSelection();
            if (selection == null)
                return;

            // Recreate visualizer
            SelectionVisualizer.getNew(selection, event.getPlayer()).create();
        }
    }

    @EventHandler
    public final void onDropItem(PlayerDropItemEvent event) {
        if (!is(event.getItemDrop().getItemStack()))
            return;

        event.getItemDrop().setItemStack(ItemStack.empty());
        Core.plugin.unregisterListeners(this);
        SelectionVisualizer vis = SelectionVisualizer.getExisting(event.getPlayer());
        if (vis != null)
            vis.remove();

        Core.text.send(event.getPlayer(), "<red>Selection cancelled due to dropping selection tool.");
        new SelectionCancelEvent(event.getPlayer()).callCoreEvent();
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (!event.getPlayer().getUniqueId().equals(user))
            return;

        Core.plugin.unregisterListeners(this);
        SelectionVisualizer vis = SelectionVisualizer.getExisting(event.getPlayer());
        if (vis != null)
            vis.remove();
        new SelectionCancelEvent(event.getPlayer()).callCoreEvent();
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (!event.getPlayer().getUniqueId().equals(user))
            return;

        Core.plugin.unregisterListeners(this);
        SelectionVisualizer vis = SelectionVisualizer.getExisting(event.getPlayer());
        if (vis != null)
            vis.remove();

        Core.text.send(event.getPlayer(), "<red>Selection cancelled due to world change.");
        new SelectionCancelEvent(event.getPlayer()).callCoreEvent();
    }

}
