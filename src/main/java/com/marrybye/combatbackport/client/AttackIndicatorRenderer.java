package com.marrybye.combatbackport.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;

import com.marrybye.combatbackport.Config;
import com.marrybye.combatbackport.api.ICombatPlayer;
import com.marrybye.combatbackport.combat.AttackIndicatorMode;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AttackIndicatorRenderer {

    public static final AttackIndicatorRenderer INSTANCE = new AttackIndicatorRenderer();

    private static final ResourceLocation CROSSHAIR_BG = new ResourceLocation(
        "combatbackport",
        "textures/gui/crosshair_attack_indicator_background.png");
    private static final ResourceLocation CROSSHAIR_PROGRESS = new ResourceLocation(
        "combatbackport",
        "textures/gui/crosshair_attack_indicator_progress.png");
    private static final ResourceLocation CROSSHAIR_FULL = new ResourceLocation(
        "combatbackport",
        "textures/gui/crosshair_attack_indicator_full.png");

    private static final ResourceLocation HOTBAR_BG = new ResourceLocation(
        "combatbackport",
        "textures/gui/hotbar_attack_indicator_background.png");
    private static final ResourceLocation HOTBAR_PROGRESS = new ResourceLocation(
        "combatbackport",
        "textures/gui/hotbar_attack_indicator_progress.png");

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (Config.attackIndicatorMode == AttackIndicatorMode.DISABLED) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        // Respect F1 (hide GUI)
        if (mc.gameSettings.hideGUI) {
            return;
        }

        if (!(mc.thePlayer instanceof ICombatPlayer)) {
            return;
        }

        ICombatPlayer combatPlayer = (ICombatPlayer) mc.thePlayer;
        float charge = combatPlayer.getCooledAttackStrength(event.partialTicks);

        ScaledResolution res = event.resolution;
        int screenWidth = res.getScaledWidth();
        int screenHeight = res.getScaledHeight();

        if (Config.attackIndicatorMode == AttackIndicatorMode.CROSSHAIR
            && event.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            renderCrosshairIndicator(mc, screenWidth, screenHeight, charge);
        } else if (Config.attackIndicatorMode == AttackIndicatorMode.HOTBAR
            && event.type == RenderGameOverlayEvent.ElementType.HOTBAR) {
                renderHotbarIndicator(mc, screenWidth, screenHeight, charge);
            }
    }

    private void renderCrosshairIndicator(Minecraft mc, int screenWidth, int screenHeight, float charge) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        if (charge < 1.0F) {
            int x = centerX - 8;
            int y = centerY + 4;

            // 1. Draw genuine background (16x4)
            mc.getTextureManager()
                .bindTexture(CROSSHAIR_BG);
            drawTexturedQuad(x, y, 16, 4, 0.0F, 0.0F, 1.0F, 1.0F);

            // 2. Draw genuine progress (16x4)
            int filledWidth = (int) (charge * 16.0F);
            if (filledWidth > 0) {
                float maxU = (float) filledWidth / 16.0F;
                mc.getTextureManager()
                    .bindTexture(CROSSHAIR_PROGRESS);
                drawTexturedQuad(x, y, filledWidth, 4, 0.0F, 0.0F, maxU, 1.0F);
            }
        } else {
            // Fully charged: if aiming at attackable entity, show genuine '+' icon
            MovingObjectPosition mop = mc.objectMouseOver;
            if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
                && mop.entityHit instanceof EntityLivingBase
                && ((EntityLivingBase) mop.entityHit).isEntityAlive()) {
                int x = centerX - 8;
                int y = centerY + 4;
                mc.getTextureManager()
                    .bindTexture(CROSSHAIR_FULL);
                drawTexturedQuad(x, y, 16, 16, 0.0F, 0.0F, 1.0F, 1.0F);
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private void renderHotbarIndicator(Minecraft mc, int screenWidth, int screenHeight, float charge) {
        if (charge >= 1.0F) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int x = screenWidth / 2 + 91 + 6;
        int y = screenHeight - 20;

        // 1. Draw genuine hotbar background (18x18)
        mc.getTextureManager()
            .bindTexture(HOTBAR_BG);
        drawTexturedQuad(x, y, 18, 18, 0.0F, 0.0F, 1.0F, 1.0F);

        // 2. Draw genuine hotbar progress from bottom to top (18x18)
        int filledHeight = (int) (charge * 18.0F);
        if (filledHeight > 0) {
            int yOffset = 18 - filledHeight;
            float minV = (float) yOffset / 18.0F;
            mc.getTextureManager()
                .bindTexture(HOTBAR_PROGRESS);
            drawTexturedQuad(x, y + yOffset, 18, filledHeight, 0.0F, minV, 1.0F, 1.0F);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private static void drawTexturedQuad(int x, int y, int width, int height, float minU, float minV, float maxU,
        float maxV) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0.0D, minU, maxV);
        tessellator.addVertexWithUV(x + width, y + height, 0.0D, maxU, maxV);
        tessellator.addVertexWithUV(x + width, y, 0.0D, maxU, minV);
        tessellator.addVertexWithUV(x, y, 0.0D, minU, minV);
        tessellator.draw();
    }
}
