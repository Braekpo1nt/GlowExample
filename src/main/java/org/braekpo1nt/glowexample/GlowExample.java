package org.braekpo1nt.glowexample;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import org.braekpo1nt.glowexample.commands.GlowCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class GlowExample extends JavaPlugin implements Listener {
    
    @Getter
    private final WhoSeesWho whoSeesWho = new WhoSeesWho();
    private final GlowManager glowManager = new GlowManager(this);
    
    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }
    
    @Override
    public void onEnable() {
        
        PacketEvents.getAPI().init();
        getServer().getPluginManager().registerEvents(this, this);
        glowManager.start();
        for (Player player : getServer().getOnlinePlayers()) {
            glowManager.addPlayer(player);
        }
        
        LifecycleEventManager<Plugin> lcManager = this.getLifecycleManager();
        lcManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("glow", "glow description", new GlowCommand(this));
        });
    }
    
    public void showGlowing(UUID viewerUUID, UUID targetUUID) {
        boolean changed = whoSeesWho.show(viewerUUID, targetUUID);
        if (changed) {
            glowManager.showGlowing(viewerUUID, targetUUID);
        }
    }
    
    public void hideGlowing(UUID viewerUUID, UUID targetUUID) {
        boolean changed = whoSeesWho.hide(viewerUUID, targetUUID);
        if (changed) {
            glowManager.hideGlowing(viewerUUID, targetUUID);
        }
    }
    
    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        onPlayerJoin(player);
    }
    
    private void onPlayerJoin(Player player) {
        UUID joinedUUID = player.getUniqueId();
        // in this example, all players are always contained in the glowManager
        glowManager.addPlayer(player);
        for (Player onlinePlayer : getServer().getOnlinePlayers()) {
            UUID onlineUUID = onlinePlayer.getUniqueId();
            if (whoSeesWho.canSee(joinedUUID, onlineUUID)) {
                glowManager.showGlowing(joinedUUID, onlineUUID);
            }
            if (whoSeesWho.canSee(onlineUUID, joinedUUID)) {
                glowManager.showGlowing(onlineUUID, joinedUUID);
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // in this example all players are always contained in the glowManager
        glowManager.removePlayer(player);
    }
    
    @Override
    public void onDisable() {
        glowManager.stop();
        PacketEvents.getAPI().terminate();
    }
    
    /**
     * 
     * @param entity the entity to get the data for
     * @param glowing whether the entity should be glowing
     * @return a base entity metadata byte containing the flags representing the
     * given entity's true state, but with the given glowing flag.
     */
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
    
    /**
     * not truly complete
     * @param player the player to get the data of
     * @return 127 as a byte
     */
    public static byte getTrueSkinDisplayByte(Player player) {
        return (byte) 127;
    }
}
