package org.braekpo1nt.packetEventsTutorial.listeners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import org.braekpo1nt.packetEventsTutorial.EntityMapper;
import org.braekpo1nt.packetEventsTutorial.PacketEventsTutorial;
import org.braekpo1nt.packetEventsTutorial.WhoSeesWho;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;

public class LeaveJoinListener implements Listener {
    
    private final EntityMapper mapper;
    private final WhoSeesWho whoSeesWho;
    private final PacketEventsTutorial plugin;
    
    public LeaveJoinListener(PacketEventsTutorial plugin, EntityMapper mapper, WhoSeesWho whoSeesWho) {
        this.mapper = mapper;
        this.whoSeesWho = whoSeesWho;
        this.plugin = plugin;
//        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getLogger().info("onJoin");
        Player joiningPlayer = event.getPlayer();
        mapper.map(joiningPlayer.getUniqueId(), joiningPlayer.getEntityId());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (whoSeesWho.shouldSee(player.getUniqueId(), joiningPlayer.getUniqueId())) {
                    sendPacket(player, joiningPlayer, true);
                }
                if (whoSeesWho.shouldSee(joiningPlayer.getUniqueId(), player.getUniqueId())) {
                    sendPacket(joiningPlayer, player, true);
                }
            }
        }, 5L);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (whoSeesWho.shouldSee(player.getUniqueId(), joiningPlayer.getUniqueId())) {
                    sendPacket(player, joiningPlayer, true);
                }
                if (whoSeesWho.shouldSee(joiningPlayer.getUniqueId(), player.getUniqueId())) {
                    sendPacket(joiningPlayer, player, true);
                }
            }
        }, 20L);
    }
    
    private void sendPacket(Player viewer, Player target, boolean glowing) {
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
                target.getEntityId(),
                Collections.singletonList(new EntityData(0, EntityDataTypes.BYTE, GlowListener.getTrueEntityDataByte(target, glowing))));
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
        plugin.getLogger().info(String.format("viewer: %s, target: %s, glowing: %s", viewer.getName(), target.getName(), glowing));
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getLogger().info("onQuit");
        Player quittingPlayer = event.getPlayer();
        mapper.remove(quittingPlayer.getUniqueId(), quittingPlayer.getEntityId());
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (whoSeesWho.shouldSee(player.getUniqueId(), quittingPlayer.getUniqueId())) {
                sendPacket(player, quittingPlayer, false);
            }
            if (whoSeesWho.shouldSee(quittingPlayer.getUniqueId(), player.getUniqueId())) {
                sendPacket(quittingPlayer, player, false);
            }
        }
    }
}
