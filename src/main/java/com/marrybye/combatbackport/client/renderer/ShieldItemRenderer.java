package com.marrybye.combatbackport.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.marrybye.combatbackport.client.model.ModelShield;
import com.marrybye.combatbackport.combat.item.ItemShield;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ShieldItemRenderer implements IItemRenderer {

    private static final ResourceLocation SHIELD_BASE_TEXTURE = new ResourceLocation(
        "combatbackport",
        "textures/entity/shield_base.png");
    private static final ResourceLocation SHIELD_NOPATTERN_TEXTURE = new ResourceLocation(
        "combatbackport",
        "textures/entity/shield_base_nopattern.png");

    private final ModelShield model = new ModelShield();

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON
            || type == ItemRenderType.ENTITY
            || type == ItemRenderType.INVENTORY;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        if (type == ItemRenderType.ENTITY) {
            return helper == ItemRendererHelper.ENTITY_ROTATION || helper == ItemRendererHelper.ENTITY_BOBBING;
        }
        if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            return helper == ItemRendererHelper.EQUIPPED_BLOCK;
        }
        return false;
    }

    private ResourceLocation getTexture(ItemStack item) {
        if (item != null && item.hasTagCompound()) {
            if (item.getTagCompound()
                .hasKey("BlockEntityTag")
                || item.getTagCompound()
                    .hasKey("Patterns")) {
                return SHIELD_BASE_TEXTURE;
            }
        }
        return SHIELD_NOPATTERN_TEXTURE;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(getTexture(item));

        if (type == ItemRenderType.INVENTORY) {
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.enableGUIStandardItemLighting();

            // Centered vertically and horizontally in 16x16 inventory slot
            GL11.glTranslatef(8.5F, 9.0F, 50.0F);

            // Scale: 7.5F (model height is 22 * 0.0625 * 7.5 = 10.3 pixels, fits perfectly in 16x16 slot)
            GL11.glScalef(7.5F, 7.5F, 7.5F);

            // Mojang authentic GUI display rotation from shield.json:
            GL11.glRotatef(15.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(-25.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-5.0F, 0.0F, 0.0F, 1.0F);

            // Upright rotation without negative scaling to keep proper lighting & normals
            GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);

            model.render();

            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
            return;
        }

        GL11.glPushMatrix();

        // Alpha testing and blending so handle hole is transparent
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            // Undo Forge EQUIPPED_BLOCK translation (-0.5, -0.5, -0.5) to reach exact hand pivot
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);

            // Undo ItemRenderer's glRotatef(45.0F, 0.0F, 1.0F, 0.0F) immediately to restore camera-aligned axes
            GL11.glRotatef(-45.0F, 0.0F, 1.0F, 0.0F);

            EntityLivingBase entity = data.length > 1 && data[1] instanceof EntityLivingBase
                ? (EntityLivingBase) data[1]
                : Minecraft.getMinecraft().thePlayer;
            boolean isBlocking = false;
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                isBlocking = player.isUsingItem()
                    && (player.getItemInUse() == item || (player.getItemInUse() != null && player.getItemInUse()
                        .getItem() instanceof ItemShield));
            }

            if (isBlocking) {
                // Authentic 1.9+ firstperson blocking pose:
                // Raised directly in front of the player's face, facing forward towards the crosshair
                GL11.glTranslatef(-1.25F, 0.70F, 0.30F);
                GL11.glRotatef(-5.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(10.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                GL11.glScalef(2.6F, 2.6F, 2.6F);
            } else {
                // Authentic 1.9+ firstperson idle pose:
                // Held clearly visible in the lower right area of the screen, upright
                GL11.glTranslatef(-0.15F, -0.10F, 0.10F);
                GL11.glRotatef(15.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(25.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                GL11.glScalef(2.4F, 2.4F, 2.4F);
            }

            model.render();

        } else if (type == ItemRenderType.EQUIPPED) {
            // Undo Forge EQUIPPED_BLOCK translation (-0.5, -0.5, -0.5) to reach 2D item anchor
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);

            // Undo RenderBiped flat 2D item rotations in exact reverse order
            GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(-60.0F, 0.0F, 0.0F, 1.0F);

            // Undo RenderBiped 0.375F scale factor
            GL11.glScalef(1.0F / 0.375F, 1.0F / 0.375F, 1.0F / 0.375F);

            // Undo RenderBiped flat 2D item translation to return to exact bipedRightArm forearm pivot
            GL11.glTranslatef(-0.25F, -0.1875F, 0.1875F);

            EntityLivingBase entity = data.length > 1 && data[1] instanceof EntityLivingBase
                ? (EntityLivingBase) data[1]
                : null;
            boolean isBlocking = false;
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                isBlocking = player.isUsingItem()
                    && (player.getItemInUse() == item || (player.getItemInUse() != null && player.getItemInUse()
                        .getItem() instanceof ItemShield));
            }

            if (isBlocking) {
                // 3rd person blocking: held across chest facing forward against attacks
                GL11.glTranslatef(0.1F, -0.05F, -0.15F);
                GL11.glRotatef(55.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(20.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
            } else {
                // 3rd person idle: strapped to outer side of forearm
                // Plate faces outwards to the right (+90 yaw), top points up towards elbow/shoulder
                GL11.glTranslatef(-0.02F, -0.05F, 0.0F);
                GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            }

            model.render();

        } else if (type == ItemRenderType.ENTITY) {
            GL11.glTranslatef(0.0F, 0.35F, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            GL11.glScalef(0.8F, 0.8F, 0.8F);
            model.render();
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
}
