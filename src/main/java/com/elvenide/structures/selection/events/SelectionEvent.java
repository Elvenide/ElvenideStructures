package com.elvenide.structures.selection.events;

import com.elvenide.structures.selection.Selection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SelectionEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Selection selection;
    private final Player selector;

    public SelectionEvent(Selection selection, Player selector) {
        this.selection = selection;
        this.selector = selector;
    }

    public Selection selection() {
        return selection;
    }

    public Player selector() {
        return selector;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
