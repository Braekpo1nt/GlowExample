package org.braekpo1nt.packetEventsTutorial.commands;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRemoveEntityEffect;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.packetEventsTutorial.PacketEventsTutorial;
import org.braekpo1nt.packetEventsTutorial.WhoSeesWho;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;

public class GlowCommand implements BasicCommand {
    
    private final PacketEventsTutorial plugin;
    private final WhoSeesWho whoSeesWho;
    
    public GlowCommand(PacketEventsTutorial plugin, WhoSeesWho whoSeesWho) {
        this.plugin = plugin;
        this.whoSeesWho = whoSeesWho;
    }
    
    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        if (!(stack.getSender() instanceof Player player)) {
            return;
        }
        if (args.length != 3) {
            player.sendMessage("/glow <viewer> <target> <true|false>");
            return;
        }
        
        String viewerName = args[0];
        Player viewer = plugin.getServer().getPlayer(viewerName);
        if (viewer == null) {
            player.sendMessage(Component.empty()
                    .append(Component.text(viewerName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" could not be found"))
            );
            return;
        }
        
        String targetName = args[1];
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            player.sendMessage(Component.empty()
                    .append(Component.text(targetName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" could not be found"))
            );
            return;
        }
        
        boolean shouldGlow = Boolean.parseBoolean(args[2]);
        
        if (shouldGlow) {
            boolean changed = whoSeesWho.show(viewer.getUniqueId(), target.getEntityId());
            if (changed) {
                WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
                        target.getEntityId(), 
                        Collections.singletonList(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x40)));
                PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
            }
        } else {
            boolean changed = whoSeesWho.hide(viewer.getUniqueId(), target.getEntityId());
            if (changed) {
                WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
                        target.getEntityId(),
                        Collections.singletonList(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x00)));
                PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
            }
        }
        
    }
}
