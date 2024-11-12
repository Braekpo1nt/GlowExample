package org.braekpo1nt.glowexample.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.UserLoginEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import org.braekpo1nt.glowexample.GlowExample;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GlowListener implements PacketListener, Listener {
    
    private final GlowExample plugin;
    /**
     * Maps each player's Entity ID to their UUID, so that we don't have to switch threads during
     * the packet listener in order to see which entity it's referencing.
     */
    private final Map<Integer, UUID> mapper = new HashMap<>();
    
    
    /**
     * Initializes the {@link #mapper} with all online players' entity ids
     * 
     * @param plugin the plugin
     */
    public GlowListener(GlowExample plugin) {
        this.plugin = plugin;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            mapper.put(player.getEntityId(), player.getUniqueId());
            plugin.getLogger().info(String.format("player %s's UUID is %s, entityID is %d", player.getName(), player.getUniqueId(), player.getEntityId()));
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Map the logging-in player's entity id to their UUID in the {@link #mapper}
     * @param event the event
     */
    @Override
    public void onUserLogin(UserLoginEvent event) {
        Player player = event.getPlayer();
        mapper.put(player.getEntityId(), player.getUniqueId());
        plugin.getLogger().info(String.format("Login: added mapping for %s->%s", player.getEntityId(), player.getUniqueId()));
    }
    
    /**
     * Remove the existing mapping of the player's entity id from the {@link #mapper}
     * @param event the event
     */
    @EventHandler
    public void onUserDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        mapper.remove(player.getEntityId());
        plugin.getLogger().info(String.format("Disconnect: removed mapping for %s %s->%s", player.getName(), player.getEntityId(), player.getUniqueId()));
    }
    
    /**
     * Detects {@link PacketType.Play.Server#ENTITY_METADATA} packets, checks if the receiving client
     * should see the subject entity's glow effect, and modifies the packet if so.
     * @param event the event
     */
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Server.ENTITY_METADATA)) {
            UUID viewerUUID = event.getUser().getUUID();
            if (!plugin.getWhoSeesWho().containsViewer(viewerUUID)) {
                // if this packet's receiver client isn't in the whoSeesWho manager, then we don't need to proceed
                return;
            }
            WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);
            UUID targetUUID = mapper.get(packet.getEntityId());
            if (targetUUID == null) {
                // if this packet's subject is not in the mapper, then it's not a player, and we don't need to proceed
                return;
            }
            if (!plugin.getWhoSeesWho().canSee(viewerUUID, targetUUID)) {
                // if the receiving client can't see the subject of this ENTITY_METADATA packet's glow effect,
                // then we don't need to proceed
                return;
            }
            // at this point, we're making changes to the packet, so mark it to be re-encoded
            event.markForReEncode(true);
            List<EntityData> entityMetadata = packet.getEntityMetadata();
            EntityData baseEntityData = entityMetadata.stream().filter(entityData -> entityData.getIndex() == 0 && entityData.getType() == EntityDataTypes.BYTE).findFirst().orElse(null);
            if (baseEntityData == null) {
                // if there is no existing base entity data, then we just need to add ours with the "glowing"
                // flag set to true (hence index zero set to 0x40)
                entityMetadata.add(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x40));
            } else {
                // if the base entity data is included in this packet, we need to make sure that the "glowing"
                // flag is set to true
                byte flags = (byte) baseEntityData.getValue();
                flags |= (byte) 0x40;
                baseEntityData.setValue(flags);
            }
            plugin.getLogger().info("glow packet modified");
        }
    }
    
}
