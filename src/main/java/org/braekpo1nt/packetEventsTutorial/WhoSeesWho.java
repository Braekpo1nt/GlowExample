package org.braekpo1nt.packetEventsTutorial;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WhoSeesWho {
    private final Map<UUID, Viewer> viewers = new HashMap<>();
    
    public boolean containsViewer(@NotNull UUID viewerUUID) {
        return viewers.containsKey(viewerUUID);
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
}
