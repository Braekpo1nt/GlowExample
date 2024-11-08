package org.braekpo1nt.packetEventsTutorial.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.packetEventsTutorial.PacketEventsTutorial;
import org.bukkit.entity.Player;


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
        
    }
}
