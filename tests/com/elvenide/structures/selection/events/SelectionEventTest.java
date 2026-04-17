package com.elvenide.structures.selection.events;

import com.elvenide.structures.selection.Selection;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class SelectionEventTest {

    @Test
    void testSelectionEvent() {
        Selection selection = mock(Selection.class);
        Player player = mock(Player.class);
        SelectionEvent event = new SelectionEvent(selection, player);
        assertEquals(selection, event.selection());
        assertEquals(player, event.selector());
    }

    @Test
    void testSelectionCancelEvent() {
        Player player = mock(Player.class);
        SelectionCancelEvent event = new SelectionCancelEvent(player);
        assertEquals(player, event.player());
    }
}
