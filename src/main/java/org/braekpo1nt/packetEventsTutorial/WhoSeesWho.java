package org.braekpo1nt.packetEventsTutorial;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
    
    private void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Server.ENTITY_METADATA)) {
            User viewer = event.getUser();

            // Check if the packet is about the target player
            WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);
            List<EntityData> entityMetadata = packet.getEntityMetadata();
            EntityData baseEntity = entityMetadata.stream()
                    .filter(entityData -> entityData.getIndex() == 0 && entityData.getType() == EntityDataTypes.BYTE)
                    .findFirst()
                    .orElse(null);
            
            if (baseEntity != null) {
                byte flags = (byte) baseEntity.getValue();
                flags |= (byte) 0x40;
                baseEntity.setValue(flags);
                packet.setEntityMetadata(entityMetadata);
            }
        }
    }
    
    public @Nullable Set<UUID> getTargets(@NotNull UUID viewerUUID) {
        Viewer viewer = viewers.get(viewerUUID);
        if (viewer == null) {
            return null;
        }
        return viewer.getTargets();
    }
}
