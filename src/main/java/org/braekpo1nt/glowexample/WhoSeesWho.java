package org.braekpo1nt.glowexample;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class WhoSeesWho {
    private final Map<UUID, Viewer> viewers = new HashMap<>();
    
    public boolean containsViewer(@NotNull UUID viewerUUID) {
        return viewers.containsKey(viewerUUID);
    }
    
    public boolean containsTarget(@NotNull UUID targetUUID) {
        for (Viewer viewer : viewers.values()) {
            if (viewer.getTargets().contains(targetUUID)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @param playerUUID the UUID to check
     * @return true if the given UUID is that of a viewer or a target
     */
    public boolean contains(@NotNull UUID playerUUID) {
        return containsViewer(playerUUID) || containsTarget(playerUUID);
    }
    
    public boolean show(@NotNull UUID viewerUUID, @NotNull UUID target) {
        viewers.putIfAbsent(viewerUUID, new Viewer(viewerUUID));
        Viewer viewer = viewers.get(viewerUUID);
        return viewer.show(target);
    }
    
    public boolean hide(@NotNull UUID viewerUUID, @NotNull UUID target) {
        Viewer viewer = viewers.get(viewerUUID);
        if (viewer == null) {
            return false;
        }
        return viewer.hide(target);
    }
    
    public boolean canSee(@NotNull UUID viewerUUID, @NotNull UUID target) {
        Viewer viewer = viewers.get(viewerUUID);
        if (viewer == null) {
            return false;
        }
        return viewer.canSee(target);
    }
    
    public void clear() {
        viewers.clear();
    }
}
