package org.braekpo1nt.packetEventsTutorial.listeners;

import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.braekpo1nt.packetEventsTutorial.PacketEventsTutorial;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class GlowListener implements PacketListener {
    
    private final PacketEventsTutorial plugin;
    
    public GlowListener(PacketEventsTutorial plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onUserLogin(UserLoginEvent event) {
        plugin.getLogger().info("user logged in");
    }
    
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Server.ENTITY_METADATA)) {
            if (!plugin.getWhoSeesWho().containsViewer(event.getUser().getUUID())) {
                return;
            }
            plugin.getLogger().info("viewer is contained");
        }
    }
    
}
