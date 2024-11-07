package org.braekpo1nt.packetEventsTutorial;

import java.util.*;

public class WhoSeesWho {
    
    /**
     * A map of each player's UUID to the list of Entity IDs of the players who they should see glowing
     */
    private final Map<UUID, List<Integer>> canSee = new HashMap<>();
    
    /**
     * 
     * @param viewer the viewer to show the glow to
     * @param target the glow target
     * @return true if something changed, false otherwise
     */
    public boolean show(UUID viewer, int target) {
        List<Integer> targets = canSee.get(viewer);
        if (targets == null) {
            List<Integer> newTargets = new ArrayList<>();
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
    public boolean hide(UUID viewer, int target) {
        List<Integer> targets = canSee.get(viewer);
        if (targets == null) {
            return false;
        }
        return targets.remove((Object) target);
    }
    
    public boolean shouldSee(UUID viewer, int entityId) {
        List<Integer> targets = canSee.get(viewer);
        if (targets == null) {
            return false;
        }
        return targets.contains(entityId);
    }
    
    public boolean contains(UUID viewer) {
        return canSee.containsKey(viewer);
    }
}
