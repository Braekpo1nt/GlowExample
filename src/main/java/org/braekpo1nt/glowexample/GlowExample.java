package org.braekpo1nt.glowexample;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.braekpo1nt.glowexample.commands.GlowCommand;
import org.braekpo1nt.glowexample.listeners.GlowListener;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Collections;

public final class GlowExample extends JavaPlugin {
    
    private final WhoSeesWho whoSeesWho = new WhoSeesWho();
    
    public WhoSeesWho getWhoSeesWho() {
        return whoSeesWho;
    }
    
    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }
    
    @Override
    public void onEnable() {
        
        /*
         * this doesn't have to be registered before the init() method. The example just does it
         * because the init method might already trigger some events, and they don't want to miss them in
         * their particular listener. 
         * 
         * you can also unregister them
         */
        PacketEvents.getAPI().getEventManager().registerListener(new GlowListener(this), PacketListenerPriority.NORMAL);
        PacketEvents.getAPI().init();
        
        LifecycleEventManager<Plugin> lcManager = this.getLifecycleManager();
        lcManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("glow", "glow description", new GlowCommand(this));
        });
    }
    
    @Override
    public void onDisable() {
        Collection<? extends Player> onlinePlayers = getServer().getOnlinePlayers();
        for (Player viewer : onlinePlayers) {
            for (Player target : onlinePlayers) {
                if (whoSeesWho.canSee(viewer.getUniqueId(), target.getUniqueId())) {
                    whoSeesWho.hide(viewer.getUniqueId(), target.getUniqueId());
                    byte trueEntityDataByte = GlowExample.getTrueEntityDataByte(target, false);
                    WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
                            target.getEntityId(),
                            Collections.singletonList(new EntityData(0, EntityDataTypes.BYTE, trueEntityDataByte))
                    );
                    PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
                    getLogger().info(String.format("Reset glow status for %s viewing %s", viewer.getName(), target.getName()));
                }
            }
        }
        PacketEvents.getAPI().terminate();
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
