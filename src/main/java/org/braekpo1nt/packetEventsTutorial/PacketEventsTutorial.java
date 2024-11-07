package org.braekpo1nt.packetEventsTutorial;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.braekpo1nt.packetEventsTutorial.commands.GlowCommand;
import org.braekpo1nt.packetEventsTutorial.listeners.GlowListener;
import org.braekpo1nt.packetEventsTutorial.listeners.LeaveJoinListener;
import org.bukkit.entity.Player;
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
        WhoSeesWho whoSeesWho = new WhoSeesWho();
        EntityMapper mapper = new EntityMapper();
        for (Player player : getServer().getOnlinePlayers()) {
            mapper.map(player.getUniqueId(), player.getEntityId());
        }
        new LeaveJoinListener(this, mapper, whoSeesWho);
        
        PacketEvents.getAPI().getEventManager().registerListener(new GlowListener(this, whoSeesWho, mapper), PacketListenerPriority.NORMAL);
        PacketEvents.getAPI().init();
        
        
        LifecycleEventManager<Plugin> lcManager = this.getLifecycleManager();
        lcManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("glow", "glow description", new GlowCommand(this, whoSeesWho));
        });
    }
    
    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }
}
