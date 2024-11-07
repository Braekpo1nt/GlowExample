package org.braekpo1nt.packetEventsTutorial.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class GlowListener implements PacketListener {
    
    private static final String TARGET_PLAYER_NAME = "rstln";
    private static final String VIEWER_PLAYER_NAME = "Braekpo1nt";
    
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        // nothing
    }
    
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Server.ENTITY_METADATA)) {
            User viewer = event.getUser();
            Player target = Bukkit.getPlayer(TARGET_PLAYER_NAME);
            
            if (viewer.getName().equalsIgnoreCase(VIEWER_PLAYER_NAME) && target != null) {
                // Check if the packet is about the target player
                WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);
                if (packet.getEntityId() == target.getEntityId()) {
                    List<EntityData> entityMetadata = packet.getEntityMetadata();
                    entityMetadata.add(new EntityData(0, EntityDataTypes.BYTE, (byte)0x40));
                    packet.setEntityMetadata(entityMetadata);
                    Bukkit.getLogger().info("modified metadata packet");
                }
            }
        }
    }
}
