package org.braekpo1nt.packetEventsTutorial.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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
        
        
    }
}
