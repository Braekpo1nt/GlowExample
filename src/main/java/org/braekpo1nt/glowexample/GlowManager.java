package org.braekpo1nt.glowexample;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;

public class GlowManager extends PacketListenerCommon implements PacketListener {
    
    @Data
    private static class PlayerData {
        private final Player player;
        /**
         * UUIDs of entities which this player should appear to glow to
         */
        private final Set<Player> viewers = new HashSet<>();
        /**
         * UUIDs of entities that should appear to glow to this player
         */
        private final Set<Player> targets = new HashSet<>();
    }
    
    /**
     * Maps each player's Entity ID to their UUID, so that we don't have to switch threads during
     * the packet listener in order to see which entity it's referencing.
     */
    private final Map<Integer, UUID> mapper = new HashMap<>();
    private final Map<UUID, PlayerData> playerDatas = new HashMap<>();
    
    public GlowManager() {
        PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.NORMAL);
    }
    
    public void stop() {
        PacketEvents.getAPI().getEventManager().unregisterListener(this);
        for (PlayerData playerData : playerDatas.values()) {
            Player target = playerData.getPlayer();
            List<EntityData> entityMetadata = getEntityMetadata(target, false);
            for (Player viewer : playerData.getViewers()) {
                sendGlowingPacket(viewer, target.getEntityId(), entityMetadata);
            }
        }
        mapper.clear();
    }
    
    /**
     * Utility method to create an EntityData with the desired glowing status.
     * For use in an ENTITY_METADATA packet.
     * @param entity the entity which may or may not glow
     * @param glowing whether the entity should be glowing
     * @return an entity metadata which indicates that the given entity is or is not glowing
     */
    private static List<EntityData> getEntityMetadata(Entity entity, boolean glowing) {
        byte trueEntityDataByte = GlowExample.getTrueEntityDataByte(entity, glowing);
        return Collections.singletonList(new EntityData(0, EntityDataTypes.BYTE, trueEntityDataByte));
    }
    
    /**
     * Send a new packet ENTITY_METADATA packet to the viewer with the given 
     * target entity ID. 
     * @param viewer the receiver of the packet
     * @param targetEntityId the entity ID of the entity which should be glowing
     * @param entityMetadata the metadata which includes a flag indicating the glowing status
     */
    private static void sendGlowingPacket(Player viewer, int targetEntityId, List<EntityData> entityMetadata) {
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
                targetEntityId,
                entityMetadata
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
    }
    
    public void addPlayer(Player player) {
        if (playerDatas.containsKey(player.getUniqueId())) {
            logUIError("Player %s already exists in this manager", player.getName());
            return;
        }
        playerDatas.put(player.getUniqueId(), new PlayerData(player));
        mapper.put(player.getEntityId(), player.getUniqueId());
    }
    
    /**
     * Show the viewer the target's glowing effect
     * @param viewerUUID the UUID of the viewer. 
     *               Must be a player contained in this manager
     * @param targetUUID the UUID of the target (the player who should glow). 
     *               Must be a player contained in this manager.
     */
    public void showGlowing(UUID viewerUUID, UUID targetUUID) {
        PlayerData viewerPlayerData = playerDatas.get(viewerUUID);
        if (viewerPlayerData == null) {
            logUIError("Viewer player with UUID %s is not in this manager", viewerUUID);
            return;
        }
        PlayerData targetPlayerData = playerDatas.get(targetUUID);
        if (targetPlayerData == null) {
            logUIError("Target player with UUID %s is not in this manager", targetUUID);
            return;
        }
        Player target = targetPlayerData.getPlayer();
        viewerPlayerData.getTargets().add(target);
        Player viewer = viewerPlayerData.getPlayer();
        targetPlayerData.getViewers().add(viewer);
        
        List<EntityData> entityMetadata = getEntityMetadata(target, true);
        sendGlowingPacket(viewer, target.getEntityId(), entityMetadata);
    }
    
    /**
     * Hide the target's glowing effect from the viewer
     * @param viewerUUID the UUID of the viewer. 
     *               Must be a player contained in this manager
     * @param targetUUID the UUID of the target (the player who should glow). 
     *               Must be a player contained in this manager.
     */
    public void hideGlowing(UUID viewerUUID, UUID targetUUID) {
        PlayerData viewerPlayerData = playerDatas.get(viewerUUID);
        if (viewerPlayerData == null) {
            logUIError("Viewer player with UUID %s is not in this manager", viewerUUID);
            return;
        }
        PlayerData targetPlayerData = playerDatas.get(targetUUID);
        if (targetPlayerData == null) {
            logUIError("Target player with UUID %s is not in this manager", targetUUID);
            return;
        }
        
        Player target = targetPlayerData.getPlayer();
        Player viewer = viewerPlayerData.getPlayer();
        
        viewerPlayerData.getTargets().remove(target);
        targetPlayerData.getViewers().remove(viewer);
        
        List<EntityData> entityMetadata = getEntityMetadata(target, false);
        sendGlowingPacket(viewer, target.getEntityId(), entityMetadata);
    }
    
    /**
     * Remove the given player from this manager. They will stop glowing and stop
     * seeing others glow.
     * @param player the player to remove
     */
    public void removePlayer(Player player) {
        PlayerData removedPlayerData = playerDatas.remove(player.getUniqueId());
        if (removedPlayerData == null) {
            logUIError("Player %s does not exist in this manager", player.getName());
            return;
        }
        mapper.remove(player.getEntityId());
        
        Player removedPlayer = removedPlayerData.getPlayer();
        List<EntityData> removedPlayerMetadata = getEntityMetadata(removedPlayer, false);
        // removed player should no longer glow. iterate through viewers
        // and update their packets:
        for (Player viewer : removedPlayerData.getViewers()) {
            PlayerData viewerPlayerData = playerDatas.get(viewer.getUniqueId());
            viewerPlayerData.getTargets().remove(removedPlayer);
            sendGlowingPacket(viewer, removedPlayer.getEntityId(), removedPlayerMetadata);
        }
        
        // removed player should no longer see glowing. iterate through targets
        // and update their packets:
        for (Player target : removedPlayerData.getTargets()) {
            PlayerData targetPlayerData = playerDatas.get(target.getUniqueId());
            targetPlayerData.getViewers().remove(removedPlayer);
            List<EntityData> targetMetadata = getEntityMetadata(target, false);
            sendGlowingPacket(removedPlayer, target.getEntityId(), targetMetadata);
        }
    }
    
    
    
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Server.ENTITY_METADATA)) {
            UUID viewerUUID = event.getUser().getUUID();
            PlayerData viewerPlayerData = playerDatas.get(viewerUUID);
            if (viewerPlayerData == null) {
                // if the receiver of the packet is not in this manager, then do not proceed
                return;
            }
            WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);
            UUID targetUUID = mapper.get(packet.getEntityId());
            if (targetUUID == null) {
                // if the packet entity is not in the mapper, then it's a player in this manager, and we don't need to proceed
                return;
            }
            PlayerData targetPlayerData = playerDatas.get(targetUUID);
            if (targetPlayerData == null) {
                // if the target is not in this manager, then do not proceed
                return;
            }
            if (!viewerPlayerData.getTargets().contains(targetPlayerData.getPlayer())) {
                // if the viewer can't see the target's glow effect, then do not proceed
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
        }
    }
    
    private static void logUIError(String message, Object... args) {
        Bukkit.getLogger().log(Level.WARNING, "[GlowExample] " + String.format(message, args), new Exception("Error occurred in the UI. Failing gracefully."));
    }
}