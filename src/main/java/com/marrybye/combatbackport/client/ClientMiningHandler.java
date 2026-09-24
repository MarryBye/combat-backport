package com.marrybye.combatbackport.client;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientMiningHandler {

    private static long lastBlockInteractionTime = 0L;
    private static long lastBlockDestroyTime = 0L;

    public static void onBlockInteracted() {
        lastBlockInteractionTime = Minecraft.getSystemTime();
    }

    public static void onBlockDestroyed() {
        lastBlockDestroyTime = Minecraft.getSystemTime();
        lastBlockInteractionTime = Minecraft.getSystemTime();
    }

    public static boolean isRecentlyInteractedWithBlock() {
        long now = Minecraft.getSystemTime();
        return (now - lastBlockInteractionTime < 450L) || (now - lastBlockDestroyTime < 450L);
    }
}
