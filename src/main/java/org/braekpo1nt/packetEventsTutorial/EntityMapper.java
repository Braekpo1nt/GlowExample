package org.braekpo1nt.packetEventsTutorial;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Maps entities to UUIDs and vice versa
 */
public class EntityMapper {
    private final Map<UUID, Integer> uuidToEntityID = new HashMap<>();
    private final Map<Integer, UUID> entityIDToUUID = new HashMap<>();
    
    /**
     * Pairs these together
     * @param uuid the uuid
     * @param entityID the entity id
     */
    public void map(@NotNull UUID uuid, @NotNull Integer entityID) {
        uuidToEntityID.put(uuid, entityID);
        entityIDToUUID.put(entityID, uuid);
    }
    
    public void remove(@NotNull UUID uuid, @NotNull Integer entityID) {
        uuidToEntityID.remove(uuid);
        entityIDToUUID.remove(entityID);
    }
    
    public @Nullable UUID getUUID(@NotNull Integer entityID) {
        return entityIDToUUID.get(entityID);
    }
    
    public @Nullable Integer getEntityID(@NotNull UUID uuid) {
        return uuidToEntityID.get(uuid);
    }
}
