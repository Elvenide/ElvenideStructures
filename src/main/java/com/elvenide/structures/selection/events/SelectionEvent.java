package com.elvenide.structures.selection.events;

import com.elvenide.core.providers.event.CoreEvent;
import com.elvenide.structures.selection.Selection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public record SelectionEvent(Selection selection, Player selector) implements CoreEvent {
}
