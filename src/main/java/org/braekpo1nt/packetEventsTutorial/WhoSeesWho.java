package org.braekpo1nt.packetEventsTutorial;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class WhoSeesWho {
    
    /**
     * A map of each player's UUID to the list of Entity IDs of the players who they should see glowing
     */
    private final Map<UUID, List<UUID>> canSee = new HashMap<>();
    
    /**
     * 
     * @param viewer the viewer to show the glow to
     * @param target the glow target
     * @return true if something changed, false otherwise
     */
    public boolean show(@NotNull UUID viewer, @NotNull UUID target) {
        List<UUID> targets = canSee.get(viewer);
        if (targets == null) {
            List<UUID> newTargets = new ArrayList<>();
            newTargets.add(target);
            canSee.put(viewer, newTargets);
            return true;
        }
        if (targets.contains(target)) {
            return false;
        }
        targets.add(target);
        return true;
    }
    
    /**
     * 
     * @param viewer the viewer to hide the glow from
     * @param target the glow target
     * @return true if something changed, false otherwise
     */
    public boolean hide(@NotNull UUID viewer, @NotNull UUID target) {
        List<UUID> targets = canSee.get(viewer);
        if (targets == null) {
            return false;
        }
        boolean removed = targets.remove(target);
        if (targets.isEmpty()) {
            canSee.remove(viewer);
        }
        return removed;
    }
    
    public boolean shouldSee(@NotNull UUID viewer, @NotNull UUID target) {
        List<UUID> targets = canSee.get(viewer);
        if (targets == null) {
            return false;
        }
        return targets.contains(target);
    }
    
    public boolean contains(@NotNull UUID viewer) {
        return canSee.containsKey(viewer);
    }
    
    public Component toComponent(PacketEventsTutorial plugin) {
        TextComponent.Builder builder = Component.text();
        for (Map.Entry<UUID, List<UUID>> entry : canSee.entrySet()) {
            Player viewer = plugin.getServer().getPlayer(entry.getKey());
            if (viewer != null) {
                builder.append(viewer.displayName());
            } else {
                builder.append(Component.text(entry.getKey().toString()));
            }
            builder.append(Component.text(": \n"));
            List<UUID> targets = entry.getValue();
            for (UUID targetUUID : targets) {
                Player target = plugin.getServer().getPlayer(targetUUID);
                builder.append(Component.text("---"));
                if (target != null) {
                    builder.append(target.displayName());
                } else {
                    builder.append(Component.text(targetUUID.toString()));
                }
                builder.append(Component.newline());
            }
        }
        return builder.build();
    }
}
