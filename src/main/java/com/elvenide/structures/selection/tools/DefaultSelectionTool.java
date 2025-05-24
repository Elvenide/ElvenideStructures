package com.elvenide.structures.selection.tools;

import com.elvenide.core.providers.item.ItemBuilder;
import com.elvenide.structures.selection.SelectionTool;

public class DefaultSelectionTool extends SelectionTool {
    @Override
    protected ItemBuilder create(ItemBuilder builder) {
        return builder;
    }
}
