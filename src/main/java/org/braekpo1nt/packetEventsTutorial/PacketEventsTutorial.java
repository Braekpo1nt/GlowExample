package org.braekpo1nt.packetEventsTutorial;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.braekpo1nt.packetEventsTutorial.commands.GlowCommand;
import org.braekpo1nt.packetEventsTutorial.listeners.GlowListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class PacketEventsTutorial extends JavaPlugin {
    
    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }
    
    @Override
    public void onEnable() {
        PacketEvents.getAPI().getEventManager().registerListener(new GlowListener(), PacketListenerPriority.NORMAL);
        PacketEvents.getAPI().init();
        
        LifecycleEventManager<Plugin> lcManager = this.getLifecycleManager();
        lcManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("glow", "glow description", new GlowCommand(this));
        });
    }
    
    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }
}
