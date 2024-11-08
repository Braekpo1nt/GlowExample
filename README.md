# GlowExample

An example of how to make players have the glowing effect only for certain players using the [PacketEvents](https://github.com/retrooper/packetevents) library.

Use the `/glow <viewer> <target> <true|false>` command to make one online player appear to glow to another online player. The viewer will see the target glowing. 

This is robust enough to handle player disconnects/reconnects. However, it will forget the viewer list on restart or reload.

