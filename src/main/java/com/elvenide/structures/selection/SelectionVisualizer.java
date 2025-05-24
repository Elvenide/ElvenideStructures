package com.elvenide.structures.selection;

import com.elvenide.core.Core;
import com.elvenide.structures.ElvenideStructures;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class SelectionVisualizer {

    private static final HashMap<UUID, SelectionVisualizer> visualizers = new HashMap<>();

    private final Selection selection;
    private final Player player;
    private final ArrayList<Shulker> shulkers = new ArrayList<>();

    private SelectionVisualizer(Selection selection, Player player) {
        this.selection = selection;
        this.player = player;
    }

    public void create() {
        if (!shulkers.isEmpty())
            remove();

        Iterator<Location> iterator = selection.iterator();

        // Create visualization outline over time (50 blocks covered per tick)
        Core.tasks.builder()
            .then(task -> {
                for (int i = 0; i < 50; i++) {
                    if (!iterator.hasNext()) {
                        task.cancel();
                        return;
                    }

                    Location location = iterator.next();
                    Shulker shulker = location.getWorld().spawn(location, Shulker.class, s -> {
                        s.setGlowing(true);
                        s.setAI(false);
                        s.setInvulnerable(true);
                        s.setInvisible(true);
                        s.setNoPhysics(true);
                        s.setSilent(true);

                        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
                        Team team = board.getTeam("elvenide_structures_selection");
                        if (team == null)
                            team = board.registerNewTeam("elvenide_structures_selection");
                        team.color(NamedTextColor.GREEN);
                        team.addEntity(s);
                    });
                    shulkers.add(shulker);
                    shulker.setVisibleByDefault(false);
                    player.showEntity(ElvenideStructures.getPlugin(ElvenideStructures.class), shulker);
                }
            })
            .repeat(0L, 1L);
    }

    public void remove() {
        for (Shulker shulker : shulkers) {
            shulker.remove();
        }
        shulkers.clear();
    }

    @Contract("!null, _ -> !null")
    public static @Nullable SelectionVisualizer getNew(@Nullable Selection selection, Player player) {
        if (selection == null)
            return null;

        if (visualizers.containsKey(player.getUniqueId())) {
            visualizers.get(player.getUniqueId()).remove();
            visualizers.remove(player.getUniqueId());
        }

        return visualizers.computeIfAbsent(player.getUniqueId(), uuid -> new SelectionVisualizer(selection, player));
    }

    public static @Nullable SelectionVisualizer getExisting(Player player) {
        return visualizers.get(player.getUniqueId());
    }

}
