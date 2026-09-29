package com.marrybye.combatbackport.client.renderer;

import net.minecraft.client.Minecraft;
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
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.enableGUIStandardItemLighting();

            // Center of the 16x16 inventory slot
            GL11.glTranslatef(8.0F, 8.0F, 100.0F);

            // Scale to fit 16x16 slot nicely (model plate height is 22)
            GL11.glScalef(10.0F, 10.0F, 10.0F);

            // Mojang authentic display settings from shield.json:
            // "gui": { "rotation": [ 15, -25, -5 ], "translation": [ 2, 3, 0 ], "scale": [ 0.65, 0.65, 0.65 ] }
            GL11.glTranslatef(2.0F / 16.0F, -3.0F / 16.0F, 0.0F);
            GL11.glRotatef(15.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(-25.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-5.0F, 0.0F, 0.0F, 1.0F);

            // ModelBase renders Y downwards and Z backwards -> invert Y and Z like vanilla 1.12 TEISR
            GL11.glScalef(1.0F, -1.0F, -1.0F);

            model.render();

            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glPopMatrix();
            return;
        }

        GL11.glPushMatrix();

        if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
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
                // Counteract vanilla 1.7.10 sword block rotations in reverse order
                GL11.glRotatef(-60.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(80.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(-30.0F, 0.0F, 1.0F, 0.0F);
                GL11.glTranslatef(0.5F, -0.2F, 0.0F);

                // Authentic 1.9+ firstperson blocking pose: held upright guarding the face
                GL11.glTranslatef(-0.25F, 0.25F, -0.25F);
                GL11.glRotatef(-15.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(5.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(-5.0F, 0.0F, 0.0F, 1.0F);
                GL11.glScalef(1.1F, 1.1F, 1.1F);
            } else {
                // Authentic 1.9+ firstperson idle pose: lowered, slightly angled at side
                GL11.glTranslatef(0.25F, -0.15F, 0.0F);
                GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(10.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(5.0F, 0.0F, 0.0F, 1.0F);
                GL11.glScalef(0.95F, 0.95F, 0.95F);
            }

            // Invert Y and Z for ModelShield
            GL11.glScalef(1.0F, -1.0F, -1.0F);
            model.render();

        } else if (type == ItemRenderType.EQUIPPED) {
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
                // 3rd person blocking: strapped across chest facing outward
                GL11.glTranslatef(0.1F, 0.3F, -0.15F);
                GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(-15.0F, 1.0F, 0.0F, 0.0F);
                GL11.glScalef(0.75F, -0.75F, -0.75F);
            } else {
                // 3rd person idle: strapped to forearm facing side
                GL11.glTranslatef(0.1F, 0.35F, 0.05F);
                GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(0.75F, -0.75F, -0.75F);
            }

            model.render();

        } else if (type == ItemRenderType.ENTITY) {
            GL11.glTranslatef(0.0F, 0.5F, 0.0F);
            GL11.glScalef(0.6F, -0.6F, -0.6F);
            model.render();
        }

        GL11.glPopMatrix();
    }
}
