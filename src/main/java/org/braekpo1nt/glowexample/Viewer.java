 package org.braekpo1nt.glowexample;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Viewer {
    private final @NotNull UUID uuid;
    private final Set<UUID> targets = new HashSet<>();
    
    public Viewer(@NotNull UUID uuid) {
        this.uuid = uuid;
    }
    
    public UUID getUUID() {
        return uuid;
    }
    
    public boolean canSee(@NotNull UUID target) {
        return targets.contains(target);
    }
    
    public boolean show(@NotNull UUID target) {
        if (targets.contains(target)) {
            return false;
        }
        targets.add(target);
        return true;
    }
    
    public boolean hide(UUID target) {
        return targets.remove(target);
    }
    
    public @NotNull Set<UUID> getTargets() {
        return targets;
    }
}
