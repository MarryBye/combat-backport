package com.marrybye.combatbackport.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class CombatPacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("combatbackport");

    private static int packetId = 0;

    public static void init() {
        INSTANCE.registerMessage(PacketSweepAttack.Handler.class, PacketSweepAttack.class, packetId++, Side.CLIENT);
        INSTANCE.registerMessage(PacketResetCooldown.Handler.class, PacketResetCooldown.class, packetId++, Side.SERVER);
    }
}
