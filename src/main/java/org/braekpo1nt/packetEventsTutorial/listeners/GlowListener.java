package org.braekpo1nt.packetEventsTutorial.listeners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.braekpo1nt.packetEventsTutorial.EntityMapper;
import org.braekpo1nt.packetEventsTutorial.PacketEventsTutorial;
import org.braekpo1nt.packetEventsTutorial.WhoSeesWho;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class GlowListener implements PacketListener {
    
    private final PacketEventsTutorial plugin;
    private final WhoSeesWho whoSeesWho;
    private final EntityMapper mapper;
    
    public GlowListener(PacketEventsTutorial plugin, WhoSeesWho whoSeesWho, EntityMapper mapper) {
        this.plugin = plugin;
        this.whoSeesWho = whoSeesWho;
        this.mapper = mapper;
    }
    
//    @Override
//    public void onUserConnect(UserConnectEvent event) {
//        plugin.getLogger().info("onUserConnect ");
////        mapper.map(event.getUser().getUUID(), event.getUser().getEntityId());
//    }
    
    @Override
    public void onUserLogin(UserLoginEvent event) {
        Player joiningPlayer = event.getPlayer();
        plugin.getLogger().info(String.format("LOGIN_EVENT: %s\n%s", joiningPlayer.getName(), mapper));
        mapper.map(event.getUser().getUUID(), event.getUser().getEntityId());
        
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (whoSeesWho.shouldSee(player.getUniqueId(), joiningPlayer.getUniqueId())) {
                sendPacket(player, joiningPlayer, true);
            }
            if (whoSeesWho.shouldSee(joiningPlayer.getUniqueId(), player.getUniqueId())) {
                sendPacket(joiningPlayer, player, true);
            }
        }
    }
    
    private void sendPacket(Player viewer, Player target, boolean glowing) {
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
                target.getEntityId(),
                Collections.singletonList(new EntityData(0, EntityDataTypes.BYTE, GlowListener.getTrueEntityDataByte(target, glowing))));
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
        plugin.getLogger().info(String.format("viewer: %s, target: %s, glowing: %s", viewer.getName(), target.getName(), glowing));
    }
    
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Server.ENTITY_METADATA)) {
            User viewer = event.getUser();
            if (!whoSeesWho.contains(viewer.getUUID())) {
                return;
            }
            plugin.getLogger().info(String.format("ENTITY_METADATA: %s", mapper));
            
            // Check if the packet is about the target player
            WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);
            UUID targetUUID = mapper.getUUID(packet.getEntityId());
            if (targetUUID == null) {
                return;
            }
            if (whoSeesWho.shouldSee(viewer.getUUID(), targetUUID)) {
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
                    plugin.getLogger().info("Updated flags with glowing effect: " + flags);
                }
            }
        }
    }
    
    public static byte getTrueEntityDataByte(Entity entity, boolean glowing) {
        byte flags = 0x00;
        
        // Check if the entity is on fire
        if (entity.isVisualFire()) {
            flags |= 0x01;
        }
        
        // Check if the entity is crouching (only players can crouch)
        if (entity.isSneaking()) {
            flags |= 0x02;
        }
        
        // Check if the entity is sprinting (only players can sprint)
        if (entity instanceof Player player && player.isSprinting()) {
            flags |= 0x08;
        }
        
        // Check if the entity is swimming (only living entities can swim)
        if (entity instanceof LivingEntity livingEntity && livingEntity.isSwimming()) {
            flags |= 0x10;
        }
        
        // Check if the entity is invisible
        if (entity.isInvisible()) {
            flags |= 0x20;
        }
        
        // Check if the entity has a glowing effect
        if (glowing) {
            flags |= 0x40;
        }
        
        // Check if the entity is flying with an elytra (only players can fly with an elytra)
        if (entity instanceof LivingEntity livingEntity && livingEntity.isGliding()) {
            flags |= (byte) 0x80;
        }
        
        return flags;
    }
}
