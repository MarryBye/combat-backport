package com.marrybye.combatbackport.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import com.marrybye.combatbackport.client.model.ModelShield;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ShieldItemRenderer implements IItemRenderer {

    private static final ResourceLocation SHIELD_TEXTURE = new ResourceLocation(
        "combatbackport",
        "textures/entity/shield_base.png");
    private final ModelShield model = new ModelShield();

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON
            || type == ItemRenderType.ENTITY;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return type == ItemRenderType.ENTITY
            && (helper == ItemRendererHelper.ENTITY_ROTATION || helper == ItemRendererHelper.ENTITY_BOBBING);
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(SHIELD_TEXTURE);

        GL11.glPushMatrix();

        if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            EntityLivingBase entity = data.length > 1 && data[1] instanceof EntityLivingBase
                ? (EntityLivingBase) data[1]
                : Minecraft.getMinecraft().thePlayer;
            boolean isBlocking = false;
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                isBlocking = player.isUsingItem() && player.getItemInUse() == item;
            }

            if (isBlocking) {
                // Held up in front of face
                GL11.glTranslatef(-0.25F, 0.45F, -0.4F);
                GL11.glRotatef(25.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-10.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(5.0F, 0.0F, 0.0F, 1.0F);
                GL11.glScalef(1.0F, 1.0F, 1.0F);
            } else {
                // Idle in hand at the side
                GL11.glTranslatef(0.4F, 0.2F, -0.15F);
                GL11.glRotatef(-15.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(10.0F, 1.0F, 0.0F, 0.0F);
                GL11.glScalef(0.9F, 0.9F, 0.9F);
            }
        } else if (type == ItemRenderType.EQUIPPED) {
            EntityLivingBase entity = data.length > 1 && data[1] instanceof EntityLivingBase
                ? (EntityLivingBase) data[1]
                : null;
            boolean isBlocking = false;
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                isBlocking = player.isUsingItem() && player.getItemInUse() == item;
            }

            if (isBlocking) {
                // 3rd person blocking: held across chest
                GL11.glTranslatef(0.6F, 0.3F, 0.2F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-30.0F, 1.0F, 0.0F, 0.0F);
            } else {
                // 3rd person idle: strapped to outer forearm
                GL11.glTranslatef(0.5F, 0.4F, 0.1F);
                GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            }
            GL11.glScalef(0.8F, 0.8F, 0.8F);
        } else if (type == ItemRenderType.ENTITY) {
            GL11.glTranslatef(0.0F, 0.5F, 0.0F);
            GL11.glScalef(0.6F, 0.6F, 0.6F);
        }

        // Standard orientation for model
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        model.render();

        GL11.glPopMatrix();
    }
}
