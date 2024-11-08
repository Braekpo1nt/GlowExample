package org.braekpo1nt.packetEventsTutorial.commands;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.packetEventsTutorial.PacketEventsTutorial;
import org.bukkit.entity.Player;

import java.util.Collections;


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
        boolean changed;
        if (shouldGlow) {
            changed = plugin.getWhoSeesWho().show(viewer.getUniqueId(), target.getUniqueId());
        } else {
            changed = plugin.getWhoSeesWho().hide(viewer.getUniqueId(), target.getUniqueId());
        }
        if (changed) {
            byte trueEntityDataByte = PacketEventsTutorial.getTrueEntityDataByte(target, shouldGlow);
            WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
                    target.getEntityId(),
                    Collections.singletonList(new EntityData(0, EntityDataTypes.BYTE, trueEntityDataByte))
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
        }
    }
}
