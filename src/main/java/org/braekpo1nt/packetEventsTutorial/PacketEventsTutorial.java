package org.braekpo1nt.packetEventsTutorial;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public final class PacketEventsTutorial extends JavaPlugin {
    
    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }
    
    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();
    }
    
    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }
}
