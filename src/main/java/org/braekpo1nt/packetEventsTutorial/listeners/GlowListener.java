package org.braekpo1nt.packetEventsTutorial.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;

public class GlowListener implements PacketListener {
    
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        // nothing
    }
    
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Server.ENTITY_EFFECT)) {
            WrapperPlayServerEntityEffect packet = new WrapperPlayServerEntityEffect(event);
            
            event.getUser().sendMessage(String.format("type: %s, amplifier: %s, duration: %s, isAmbient: %s, isVisible: %s, isShowIcon: %s",
                    SpigotConversionUtil.toBukkitPotionEffectType(packet.getPotionType()),
                    packet.getEffectAmplifier(),
                    packet.getEffectDurationTicks(),
                    packet.isAmbient(),
                    packet.isVisible(),
                    packet.isShowIcon()
            ));
        }
    }
}
