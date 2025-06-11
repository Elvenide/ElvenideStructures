package com.elvenide.structures.selection.tools;

import com.elvenide.core.providers.item.ItemBuilder;
import com.elvenide.structures.selection.SelectionTool;
import org.bukkit.entity.Player;

public class DefaultSelectionTool extends SelectionTool {
    public DefaultSelectionTool(Player user) {
        super(user);
    }

    @Override
    protected ItemBuilder create(ItemBuilder builder) {
        return builder;
    }
}
