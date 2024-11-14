package org.braekpo1nt.glowexample;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GlowManager extends SimplePacketListenerAbstract {
    
    private final Logger logger;
    private final Plugin plugin;
    
    public GlowManager(Plugin plugin) {
        super(PacketListenerPriority.NORMAL);
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    @RequiredArgsConstructor
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    private static class PlayerData {
        @Getter
        private final Player player;
        /**
         * UUIDs of entities which this player should appear to glow to
         */
        private final Set<UUID> viewers = new HashSet<>();
        /**
         * UUIDs of entities that should appear to glow to this player
         */
        private final Set<UUID> targets = new HashSet<>();
        /**
         * This has a very particular use. When players first log in, the ENTITY_METADATA 
         * packet send automatically by the server does not contain the 0-index byte 
         * data (the glowing information). Thus, the default behavior of 
         * {@link #onPacketPlaySend(PacketPlaySendEvent)} is to ignore the packet. 
         * However, this results in a player who should be glowing, but is not. 
         * If the player crouches (or otherwise updates their base "Entity" metadata) 
         * then they start glowing. 
         * <br>
         * Desired behavior: Players log in and are glowing to their viewers
         * Actual behavior: Players log in and don't glow until they update their 
         * entity data (crouch, sprint, swim, fly with elytra, etc.)
         * <br>
         * Solution: if {@link #onPacketPlaySend(PacketPlaySendEvent)} recieves an 
         * ENTITY_METADATA packet for a given target, it checks to see if the viewer's 
         * PlayerData.requiresInitialUpdate(targetUUID) is true. If so, it sends a 
         * special update to ensure the player is glowing. 
         */
        private final Set<UUID> initiallyUpdatedTargets = new HashSet<>();
        
        public void addViewer(UUID viewer) {
            viewers.add(viewer);
        }
        
        public void removeViewer(UUID viewer) {
            viewers.remove(viewer);
        }
        
        public boolean canBeSeen(UUID viewer) {
            return viewers.contains(viewer);
        }
        
        public Set<UUID> getViewersList() {
            return viewers;
        }
        
        public void addTarget(UUID target) {
            targets.add(target);
        }
        
        public void removeTarget(UUID target) {
            targets.remove(target);
            initiallyUpdatedTargets.remove(target);
        }
        
        public Set<UUID> getTargetsList() {
            return targets;
        }
        
        public boolean canSee(UUID target) {
            return targets.contains(target);
        }
        
        public boolean requiresUpdate(UUID target) {
            return !initiallyUpdatedTargets.contains(target);
        }
        
        public void initialUpdate(UUID target) {
            initiallyUpdatedTargets.add(target);
        }
        
    }
    
    /**
     * Maps each player's Entity ID to their UUID, so that we don't have to switch threads during
     * the packet listener in order to see which entity it's referencing.
     */
    private final Map<Integer, UUID> mapper = new HashMap<>();
    private final Map<UUID, PlayerData> playerDatas = new HashMap<>();
    
    public void start() {
        PacketEvents.getAPI().getEventManager().registerListener(this);
    }
    
    public void stop() {
        PacketEvents.getAPI().getEventManager().unregisterListener(this);
        for (PlayerData playerData : playerDatas.values()) {
            Player target = playerData.getPlayer();
            List<EntityData> entityMetadata = getEntityMetadata(target, false);
            for (UUID viewerUUID : playerData.getViewersList()) {
                Player viewer = playerDatas.get(viewerUUID).getPlayer();
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
        
        if (viewerPlayerData.canSee(targetUUID)) {
            logUIError("Viewer with UUID %s can already see target with UUID %s glowing", viewerUUID, targetUUID);
            return;
        }
        // (no need to check the viewers because they are maintained in tandem)
        
        Player target = targetPlayerData.getPlayer();
        Player viewer = viewerPlayerData.getPlayer();
        viewerPlayerData.addTarget(targetUUID);
        targetPlayerData.addViewer(viewerUUID);
        
        List<EntityData> entityMetadata = getEntityMetadata(target, true);
        entityMetadata = new ArrayList<>(entityMetadata);
        entityMetadata.add(new EntityData(17, EntityDataTypes.BYTE, GlowExample.getTrueSkinDisplayByte(target)));
        entityMetadata.add(new EntityData(9, EntityDataTypes.FLOAT, ((float) target.getHealth())));
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
        
        if (!viewerPlayerData.canSee(targetUUID)) {
            logUIError("Viewer with UUID %s doesn't see target with UUID %s glowing", viewerUUID, targetUUID);
            return;
        }
        // (no need to check the viewers because they are maintained in tandem)
        
        Player target = targetPlayerData.getPlayer();
        Player viewer = viewerPlayerData.getPlayer();
        viewerPlayerData.removeTarget(targetUUID);
        targetPlayerData.removeViewer(viewerUUID);
        
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
        UUID removedUUID = removedPlayer.getUniqueId();
        for (UUID viewerUUID : removedPlayerData.getViewersList()) {
            PlayerData viewerPlayerData = playerDatas.get(viewerUUID);
            Player viewer = viewerPlayerData.getPlayer();
            viewerPlayerData.removeTarget(removedUUID);
            sendGlowingPacket(viewer, removedPlayer.getEntityId(), removedPlayerMetadata);
        }
        
        // removed player should no longer see glowing. iterate through targets
        // and update their packets:
        for (UUID targetUUID : removedPlayerData.getTargetsList()) {
            PlayerData targetPlayerData = playerDatas.get(targetUUID);
            Player target = targetPlayerData.getPlayer();
            targetPlayerData.removeViewer(removedUUID);
            List<EntityData> targetMetadata = getEntityMetadata(target, false);
            sendGlowingPacket(removedPlayer, target.getEntityId(), targetMetadata);
        }
    }
    
    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Server.ENTITY_METADATA)) {
            
            UUID viewerUUID = event.getUser().getUUID();
            PlayerData viewerPlayerData = playerDatas.get(viewerUUID);
            if (viewerPlayerData == null) {
                // if the receiver of the packet is not in this manager, then do not proceed
                return;
            }
            WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);
            int entityId = packet.getEntityId();
            Player player = event.getPlayer();
            UUID targetUUID = mapper.get(entityId);
            if (targetUUID == null) {
                // if the packet entity is not in the mapper, then it's a player in this manager, and we don't need to proceed
                logIfPlayer(player, entityId, "entityId %s doesn't have a UUID mapping", entityId);
                return;
            }
            if (!viewerPlayerData.canSee(targetUUID)) {
                // if the viewer can't see the target's glow effect, then do not proceed
                logIfPlayer(player, entityId, "%s can't see %d", player.getName(), entityId);
                return;
            }
            List<EntityData> entityMetadata = packet.getEntityMetadata();
            EntityData baseEntityData = entityMetadata.stream().filter(entityData -> entityData.getIndex() == 0 && entityData.getType() == EntityDataTypes.BYTE).findFirst().orElse(null);
            if (baseEntityData == null) {
                if (viewerPlayerData.requiresUpdate(targetUUID)) {
                    event.markForReEncode(true);
                    //TODO: include true byte data
                    entityMetadata.add(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x40));
                    viewerPlayerData.initialUpdate(targetUUID);
                    logIfPlayer(player, entityId, "required initial update");
                }
                else {
                    logIfPlayer(player, entityId, "no update needed");
                }
                return;
            }
            // at this point, we're making changes to the packet, so mark it to be re-encoded
            event.markForReEncode(true);
            // if the base entity data is included in this packet, 
            // we need to make sure that the "glowing" flag is set to true
            byte flags = (byte) baseEntityData.getValue();
            flags |= (byte) 0x40;
            baseEntityData.setValue(flags);
            logger.info("glowing packet modified");
        }
    }
    
    private void logIfPlayer(Player receiver, int entityId, String message, Object... args) {
        if (receiver.getEntityId() == entityId) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Entity entity = receiver.getWorld().getEntities().stream().filter(e -> e.getEntityId() == entityId).findFirst().orElse(null);
            if (!(entity instanceof Player player)) {
                return;
            }
            logger.info(String.format("%s - Player:[%s,%s]", String.format(message, args), player.getName(), player.getEntityId()));
        });
    }
    
    private void logUIError(String message, Object... args) {
        logger.log(Level.WARNING, "[GlowExample] Error occurred in the UI. Failing gracefully.", new Exception(String.format(message, args)));
    }
}
