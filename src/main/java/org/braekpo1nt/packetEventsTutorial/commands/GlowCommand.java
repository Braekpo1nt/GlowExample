package org.braekpo1nt.packetEventsTutorial.commands;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.data.EntityMetadataProvider;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRemoveEntityEffect;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.packetEventsTutorial.PacketEventsTutorial;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class GlowCommand implements BasicCommand {
    
    private final PacketEventsTutorial plugin;
    
    public GlowCommand(PacketEventsTutorial plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        if (!(stack.getSender() instanceof Player player)) {
            return;
        }
        
        if (args.length != 2) {
            player.sendMessage("/glow <playerName> <true|false>");
            return;
        }
        
        String name = args[0];
        Player glowingPlayer = plugin.getServer().getPlayer(name);
        if (glowingPlayer == null) {
            player.sendMessage(Component.empty()
                    .append(Component.text(name)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" could not be found"))
            );
            return;
        }
        boolean shouldGlow = Boolean.parseBoolean(args[1]);
        
        if (shouldGlow) {
            // add glow
            WrapperPlayServerEntityEffect packet = new WrapperPlayServerEntityEffect(
                    glowingPlayer.getEntityId(),
                    SpigotConversionUtil.fromBukkitPotionEffectType(PotionEffectType.GLOWING),
                    0,
                    2000,
                    (byte) 0x06
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        } else {
            // remove glow
            WrapperPlayServerRemoveEntityEffect packet = new WrapperPlayServerRemoveEntityEffect(
                    glowingPlayer.getEntityId(),
                    SpigotConversionUtil.fromBukkitPotionEffectType(PotionEffectType.GLOWING)
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        }
        
    }
}
