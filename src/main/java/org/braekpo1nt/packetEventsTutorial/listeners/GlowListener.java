package org.braekpo1nt.packetEventsTutorial.listeners;

import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import org.braekpo1nt.packetEventsTutorial.PacketEventsTutorial;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GlowListener implements PacketListener, Listener {
    
    private final PacketEventsTutorial plugin;
    private final Map<Integer, UUID> mapper = new HashMap<>();
    
    
    public GlowListener(PacketEventsTutorial plugin) {
        this.plugin = plugin;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            mapper.put(player.getEntityId(), player.getUniqueId());
            plugin.getLogger().info(String.format("player %s's UUID is %s, entityID is %d", player.getName(), player.getUniqueId(), player.getEntityId()));
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public void onUserLogin(UserLoginEvent event) {
        Player player = event.getPlayer();
        mapper.put(player.getEntityId(), player.getUniqueId());
        plugin.getLogger().info(String.format("Login: added mapping for %s->%s", player.getEntityId(), player.getUniqueId()));
    }
    
    @EventHandler
    public void onUserDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        mapper.remove(player.getEntityId());
        plugin.getLogger().info(String.format("Disconnect: removed mapping for %s %s->%s", player.getName(), player.getEntityId(), player.getUniqueId()));
    }
    
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Server.ENTITY_METADATA)) {
            if (!plugin.getWhoSeesWho().containsViewer(event.getUser().getUUID())) {
                return;
            }
            WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);
            UUID targetUUID = mapper.get(packet.getEntityId());
            if (targetUUID == null) {
                return;
            }
            List<EntityData> entityMetadata = packet.getEntityMetadata();
            EntityData baseEntityData = entityMetadata.stream().filter(entityData -> entityData.getIndex() == 0).findFirst().orElse(null);
            if (baseEntityData == null) {
                entityMetadata.add(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x40));
            } else {
                baseEntityData.setValue(((byte) baseEntityData.getValue()) | 0x40);
            }
            packet.setEntityMetadata(entityMetadata); // TODO: make sure this is needed
            plugin.getLogger().info("viewer is contained");
        }
    }
    
}
