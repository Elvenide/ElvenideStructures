package com.elvenide.structures.selection.events;

import com.elvenide.core.providers.event.CoreEvent;
import org.bukkit.entity.Player;

public record SelectionCancelEvent(Player player) implements CoreEvent {
}
