package com.marrybye.combatbackport.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientMiningHandler {

    private static long lastBlockInteractionTime = 0L;
    private static long lastBlockDestroyTime = 0L;
    private static boolean currentSwingIsMining = false;
    private static int lastSwingProgressInt = -1;

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

    public static boolean isMiningSwing(Minecraft mc) {
        if (mc == null || mc.thePlayer == null) {
            return false;
        }

        // Entity attacks are always combat swings
        if (mc.objectMouseOver != null
            && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            currentSwingIsMining = false;
            return false;
        }

        boolean isHitting = mc.gameSettings != null && mc.gameSettings.keyBindAttack.getIsKeyPressed()
            && mc.objectMouseOver != null
            && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;

        boolean isRecent = isRecentlyInteractedWithBlock();

        int swingInt = mc.thePlayer.swingProgressInt;
        if (mc.thePlayer.isSwingInProgress) {
            // Lock swing mode at the start of a swing cycle
            if (swingInt <= 0 || swingInt < lastSwingProgressInt) {
                currentSwingIsMining = isHitting || isRecent;
            }
        } else {
            currentSwingIsMining = isHitting || isRecent;
        }

        lastSwingProgressInt = swingInt;
        return currentSwingIsMining;
    }
}
