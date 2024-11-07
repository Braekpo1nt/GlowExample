package org.braekpo1nt.packetEventsTutorial.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import org.braekpo1nt.packetEventsTutorial.PacketEventsTutorial;
import org.braekpo1nt.packetEventsTutorial.WhoSeesWho;

import java.util.List;

public class GlowListener implements PacketListener {
    
    private static final String TARGET_PLAYER_NAME = "rstln";
    private final PacketEventsTutorial plugin;
    private final WhoSeesWho whoSeesWho;
    
    public GlowListener(PacketEventsTutorial plugin, WhoSeesWho whoSeesWho) {
        this.plugin = plugin;
        this.whoSeesWho = whoSeesWho;
    }
    
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Server.ENTITY_METADATA)) {
            User viewer = event.getUser();
            if (!whoSeesWho.contains(viewer.getUUID())) {
                return;
            }
            
            // Check if the packet is about the target player
            WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);
            if (whoSeesWho.shouldSee(viewer.getUUID(), packet.getEntityId())) {
                List<EntityData> entityMetadata = packet.getEntityMetadata();
                EntityData baseEntity = entityMetadata.stream()
                        .filter(entityData -> entityData.getIndex() == 0 && entityData.getType() == EntityDataTypes.BYTE)
                        .findFirst()
                        .orElse(null);
                
                if (baseEntity == null) {
                    // If there's no existing flag entry, add a new one with just the glowing effect
                    byte newFlags = 0x40;
                    entityMetadata.add(new EntityData(0, EntityDataTypes.BYTE, newFlags));
                    packet.setEntityMetadata(entityMetadata);
                    plugin.getLogger().info("Added new glowing effect flag (0x40).");
                } else {
                    // Read and modify the existing flags
                    byte flags = (byte) baseEntity.getValue();
                    // Check if glowing bit is set, if not, add it
                    boolean isGlowing = (flags & 0x40) != 0;
                    if (!isGlowing) {
                        flags |= 0x40;
                        baseEntity.setValue(flags);
                        packet.setEntityMetadata(entityMetadata);
                        plugin.getLogger().info("Updated flags with glowing effect: " + flags);
                    }
                }
            }
        }
    }
}
