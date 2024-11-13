package org.braekpo1nt.glowexample;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;

public class StringUtils {
    
    public static String baseEntityData(byte flags) {
        return  "{\nIs on fire: " + flagIsPresent(flags, (byte) 0x01) +
                ",\nIs crouching: " + flagIsPresent(flags, (byte) 0x02) +
                ",\nUnused (previously riding): " + flagIsPresent(flags, (byte) 0x04) +
                ",\nIs sprinting: " + flagIsPresent(flags, (byte) 0x08) +
                ",\nIs on swimming: " + flagIsPresent(flags, (byte) 0x10) +
                ",\nIs on visible: " + flagIsPresent(flags, (byte) 0x20) +
                ",\nHas glowing effect: " + flagIsPresent(flags, (byte) 0x40) +
                ",\nIs flying with elytra: " + flagIsPresent(flags, (byte) 0x80)
                + "\n}";
    }
    
    public static String skinDisplay(byte flags) {
        return  "{\nCape: " + flagIsPresent(flags, (byte) 0x01) +
                ",\nJacket: " + flagIsPresent(flags, (byte) 0x02) +
                ",\nLeft sleeve: " + flagIsPresent(flags, (byte) 0x04) +
                ",\nRight sleeve: " + flagIsPresent(flags, (byte) 0x08) +
                ",\nLeft pants leg: " + flagIsPresent(flags, (byte) 0x10) +
                ",\nRight pants leg: " + flagIsPresent(flags, (byte) 0x20) +
                ",\nHat: " + flagIsPresent(flags, (byte) 0x40) +
                ",\nUnused: " + flagIsPresent(flags, (byte) 0x80)
                + "\n}";
    }
    
    public static boolean flagIsPresent(byte flags, byte flag) {
        return (flag & flags) == (byte) 0x01;
    }
    
    public static String stringify(WrapperPlayServerEntityMetadata packet) {
        StringBuilder s = new StringBuilder();
        s
                .append("\nEntityID: ")
                .append(packet.getEntityId())
                .append('\n');
        for (EntityData entityData : packet.getEntityMetadata()) {
            try {
                s.append(stringify(entityData))
                        .append('\n');
            } catch (ClassCastException e) {
                s.append(entityData.getIndex())
                        .append("couldn't be cast. Needed ")
                        .append(entityData.getType().getName())
                        .append('\n');
            }
        }
        return s.toString();
    }
    
    private static String stringify(EntityData entityData) {
        StringBuilder s = new StringBuilder();
        s.append(entityData.getIndex())
                .append(" - ");
        switch(entityData.getIndex()) {
            // Entity
            case 0 -> s.append("Base Entity Data: ")
                    .append(baseEntityData((byte) entityData.getValue()));
            case 1 -> s.append("Air ticks: ").append((int) entityData.getValue());
            case 2 -> s.append("Custom Name: ").append(entityData.getValue());
            case 3 -> s.append("Visible custom name: ").append((boolean) entityData.getValue());
            case 4 -> s.append("Is silent: ").append((boolean) entityData.getValue());
            case 5 -> s.append("Has no gravity: ").append((boolean) entityData.getValue());
            case 6 -> s.append("Pose: ").append(entityData.getValue());
            case 7 -> s.append("Ticks frozen in powdered snow: ").append((int) entityData.getValue());
            // Living Entity
            case 8 -> s.append("living entity byte: ").append((byte) entityData.getValue());
            case 9 -> s.append("Health: ").append((float) entityData.getValue());
            case 10 -> s.append("Potion effect color: ").append((int) entityData.getValue());
            case 11 -> s.append("Ambient: ").append((boolean) entityData.getValue());
            case 12 -> s.append("Arrows #: ").append((int) entityData.getValue());
            case 14 -> s.append("Bed location: ").append(entityData.getValue());
            // Player
            case 15 -> s.append("Addl Hearts: ").append((float) entityData.getValue());
            case 16 -> s.append("Score: ").append((int) entityData.getValue());
            case 17 -> s.append("Skin display: ")
                    .append(skinDisplay((byte) entityData.getValue()));
            case 18 -> s.append("Main hand: ").append((byte) entityData.getValue());
            case 19 -> s.append("Left shoulder parrot: ").append(entityData.getValue());
            case 20 -> s.append("Right shoulder parrot: ").append(entityData.getValue());
        }
        return s.toString();
    }
}
