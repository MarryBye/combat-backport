package com.marrybye.combatbackport.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemCloth;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.marrybye.combatbackport.Config;
import com.marrybye.combatbackport.api.ICombatPlayer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    private static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");

    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    private float equippedProgress;

    @Shadow
    private float prevEquippedProgress;

    @Shadow
    private ItemStack itemToRender;

    @Shadow
    private int equippedItemSlot;

    @Shadow
    public abstract void renderItem(EntityLivingBase p_78443_1_, ItemStack p_78443_2_, int p_78443_3_);

    @Unique
    private boolean combatbackport$isAxe(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }
        if (stack.getItem() instanceof ItemAxe) {
            return true;
        }
        java.util.Set<String> toolClasses = stack.getItem()
            .getToolClasses(stack);
        if (toolClasses != null && toolClasses.contains("axe")) {
            return true;
        }
        String name = stack.getItem()
            .getUnlocalizedName();
        if (name != null) {
            name = name.toLowerCase();
            return name.contains("axe") || name.contains("hatchet")
                || name.contains("mattock")
                || name.contains("cleaver")
                || name.contains("tomahawk")
                || name.contains("lumberaxe")
                || name.contains("battleaxe");
        }
        String simpleName = stack.getItem()
            .getClass()
            .getSimpleName()
            .toLowerCase();
        return simpleName.contains("axe") || simpleName.contains("hatchet")
            || simpleName.contains("mattock")
            || simpleName.contains("cleaver")
            || simpleName.contains("tomahawk")
            || simpleName.contains("battleaxe");
    }

    /**
     * @author MarryBye & Gemini AI
     * @reason Authentic Minecraft 1.9+ attack cooldown and weapon equip rise animation.
     */
    @Overwrite
    public void updateEquippedItem() {
        this.prevEquippedProgress = this.equippedProgress;
        EntityClientPlayerMP entityclientplayermp = this.mc != null ? this.mc.thePlayer : null;
        ItemStack itemstack = entityclientplayermp != null && entityclientplayermp.inventory != null
            ? entityclientplayermp.inventory.getCurrentItem()
            : null;
        boolean flag = entityclientplayermp != null
            && this.equippedItemSlot == entityclientplayermp.inventory.currentItem
            && itemstack == this.itemToRender;

        if (this.itemToRender == null && itemstack == null) {
            flag = true;
        }

        if (itemstack != null && this.itemToRender != null
            && itemstack != this.itemToRender
            && itemstack.getItem() == this.itemToRender.getItem()
            && itemstack.getItemDamage() == this.itemToRender.getItemDamage()
            && ItemStack.areItemStackTagsEqual(itemstack, this.itemToRender)) {
            this.itemToRender = itemstack;
            flag = true;
        }

        float fCooldown = 1.0F;
        if (Config.enableAttackCooldown && entityclientplayermp instanceof ICombatPlayer) {
            fCooldown = ((ICombatPlayer) entityclientplayermp).getCooledAttackStrength(1.0F);
        }

        float f = 0.4F;
        float fTarget;
        if (!flag) {
            // Lower item when switching slots
            fTarget = 0.0F;
        } else if (com.marrybye.combatbackport.client.ClientMiningHandler.isMiningSwing(this.mc)) {
            // Keep tool ready during active block mining / foliage clearing
            fTarget = 1.0F;
        } else {
            // In 1.9+, target progress is cooldown^3 (both for equip rise and attack recovery)
            fTarget = (fCooldown * fCooldown * fCooldown);
        }

        float f2 = fTarget - this.equippedProgress;

        if (f2 < -f) {
            f2 = -f;
        }

        if (f2 > f) {
            f2 = f;
        }

        this.equippedProgress += f2;

        if (this.equippedProgress < 0.1F) {
            this.itemToRender = itemstack;
            if (entityclientplayermp != null && entityclientplayermp.inventory != null) {
                this.equippedItemSlot = entityclientplayermp.inventory.currentItem;
            }
        }
    }

    /**
     * @author MarryBye & Gemini AI
     * @reason Authentic Minecraft 1.9+ transformFirstPersonItem and renderItemInFirstPerson implementation,
     *         with separate handling for digging/mining and enhanced visibility for axes.
     */
    @Overwrite
    public void renderItemInFirstPerson(float partialTicks) {
        float f1 = this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks;
        EntityClientPlayerMP entityclientplayermp = this.mc != null ? this.mc.thePlayer : null;
        if (entityclientplayermp == null) return;

        boolean mining = com.marrybye.combatbackport.client.ClientMiningHandler.isMiningSwing(this.mc);

        float f2 = entityclientplayermp.prevRotationPitch
            + (entityclientplayermp.rotationPitch - entityclientplayermp.prevRotationPitch) * partialTicks;
        GL11.glPushMatrix();
        GL11.glRotatef(f2, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(
            entityclientplayermp.prevRotationYaw
                + (entityclientplayermp.rotationYaw - entityclientplayermp.prevRotationYaw) * partialTicks,
            0.0F,
            1.0F,
            0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();

        EntityPlayerSP entityplayersp = (EntityPlayerSP) entityclientplayermp;
        float f3 = entityplayersp.prevRenderArmPitch
            + (entityplayersp.renderArmPitch - entityplayersp.prevRenderArmPitch) * partialTicks;
        float f4 = entityplayersp.prevRenderArmYaw
            + (entityplayersp.renderArmYaw - entityplayersp.prevRenderArmYaw) * partialTicks;
        GL11.glRotatef((entityclientplayermp.rotationPitch - f3) * 0.1F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((entityclientplayermp.rotationYaw - f4) * 0.1F, 0.0F, 1.0F, 0.0F);

        ItemStack itemstack = this.itemToRender;
        if (itemstack != null && itemstack.getItem() instanceof ItemCloth) {
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        }

        int i = this.mc.theWorld.getLightBrightnessForSkyBlocks(
            MathHelper.floor_double(entityclientplayermp.posX),
            MathHelper.floor_double(entityclientplayermp.posY),
            MathHelper.floor_double(entityclientplayermp.posZ),
            0);
        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j / 1.0F, (float) k / 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (itemstack != null) {
            int l = itemstack.getItem()
                .getColorFromItemStack(itemstack, 0);
            float f5 = (float) (l >> 16 & 255) / 255.0F;
            float f6 = (float) (l >> 8 & 255) / 255.0F;
            float f7 = (float) (l & 255) / 255.0F;
            GL11.glColor4f(f5, f6, f7, 1.0F);
        } else {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        if (itemstack != null && itemstack.getItem() == Items.filled_map) {
            GL11.glPushMatrix();
            float fScale = 0.8F;
            float fSwing = entityclientplayermp.getSwingProgress(partialTicks);
            float fSin = MathHelper.sin(fSwing * (float) Math.PI);
            float fSinSqrt = MathHelper.sin(MathHelper.sqrt_float(fSwing) * (float) Math.PI);
            GL11.glTranslatef(
                -fSinSqrt * 0.4F,
                MathHelper.sin(MathHelper.sqrt_float(fSwing) * (float) Math.PI * 2.0F) * 0.2F,
                -fSin * 0.2F);
            float fMapPitch = 1.0F - f2 / 45.0F + 0.1F;
            if (fMapPitch < 0.0F) fMapPitch = 0.0F;
            if (fMapPitch > 1.0F) fMapPitch = 1.0F;
            fMapPitch = -MathHelper.cos(fMapPitch * (float) Math.PI) * 0.5F + 0.5F;
            GL11.glTranslatef(0.0F, 0.0F * fScale - (1.0F - f1) * 1.2F - fMapPitch * 0.5F + 0.04F, -0.9F * fScale);
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(fMapPitch * -85.0F, 0.0F, 0.0F, 1.0F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            this.mc.getTextureManager()
                .bindTexture(entityclientplayermp.getLocationSkin());

            for (int arm = 0; arm < 2; ++arm) {
                int armSide = arm * 2 - 1;
                GL11.glPushMatrix();
                GL11.glTranslatef(-0.0F, -0.6F, 1.1F * (float) armSide);
                GL11.glRotatef((float) (-45 * armSide), 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(59.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef((float) (-65 * armSide), 0.0F, 1.0F, 0.0F);
                Render render = RenderManager.instance.getEntityRenderObject(this.mc.thePlayer);
                RenderPlayer renderPlayer = (RenderPlayer) render;
                GL11.glScalef(1.0F, 1.0F, 1.0F);
                renderPlayer.renderFirstPersonArm(this.mc.thePlayer);
                GL11.glPopMatrix();
            }

            float fMapSwing = entityclientplayermp.getSwingProgress(partialTicks);
            float fMapSwingSq = MathHelper.sin(fMapSwing * fMapSwing * (float) Math.PI);
            float fMapSwingSqrt = MathHelper.sin(MathHelper.sqrt_float(fMapSwing) * (float) Math.PI);
            GL11.glRotatef(-fMapSwingSq * 20.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-fMapSwingSqrt * 20.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(-fMapSwingSqrt * 80.0F, 1.0F, 0.0F, 0.0F);
            GL11.glScalef(0.38F, 0.38F, 0.38F);
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-1.0F, -1.0F, 0.0F);
            GL11.glScalef(0.015625F, 0.015625F, 0.015625F);
            this.mc.getTextureManager()
                .bindTexture(RES_MAP_BACKGROUND);
            Tessellator tessellator = Tessellator.instance;
            GL11.glNormal3f(0.0F, 0.0F, -1.0F);
            tessellator.startDrawingQuads();
            byte b0 = 7;
            tessellator.addVertexWithUV((double) (0 - b0), (double) (128 + b0), 0.0D, 0.0D, 1.0D);
            tessellator.addVertexWithUV((double) (128 + b0), (double) (128 + b0), 0.0D, 1.0D, 1.0D);
            tessellator.addVertexWithUV((double) (128 + b0), (double) (0 - b0), 0.0D, 1.0D, 0.0D);
            tessellator.addVertexWithUV((double) (0 - b0), (double) (0 - b0), 0.0D, 0.0D, 0.0D);
            tessellator.draw();
            MapData mapdata = Items.filled_map.getMapData(itemstack, this.mc.theWorld);
            if (mapdata != null) {
                this.mc.entityRenderer.getMapItemRenderer()
                    .func_148250_a(mapdata, false);
            }
            GL11.glPopMatrix();
        } else if (itemstack != null) {
            GL11.glPushMatrix();

            if (entityclientplayermp.getItemInUseCount() > 0) {
                EnumAction enumaction = itemstack.getItemUseAction();
                if (enumaction == EnumAction.eat || enumaction == EnumAction.drink) {
                    float fUse = (float) entityclientplayermp.getItemInUseCount() - partialTicks + 1.0F;
                    float fDur = 1.0F - fUse / (float) itemstack.getMaxItemUseDuration();
                    float fInv = 1.0F - fDur;
                    fInv = fInv * fInv * fInv;
                    fInv = fInv * fInv * fInv;
                    fInv = fInv * fInv * fInv;
                    float fEat = 1.0F - fInv;
                    GL11.glTranslatef(
                        0.0F,
                        MathHelper.abs(MathHelper.cos(fUse / 4.0F * (float) Math.PI) * 0.1F)
                            * (float) ((double) fDur > 0.2D ? 1 : 0),
                        0.0F);
                    GL11.glTranslatef(fEat * 0.6F, -fEat * 0.5F, 0.0F);
                    GL11.glRotatef(fEat * 90.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(fEat * 10.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(fEat * 30.0F, 0.0F, 0.0F, 1.0F);
                }
            } else if (!Config.enableNewSwingAnimation || mining) {
                // Classic rhythmic mining translation when breaking blocks
                float fSwing = entityclientplayermp.getSwingProgress(partialTicks);
                float fSin = MathHelper.sin(fSwing * (float) Math.PI);
                float fSinSqrt = MathHelper.sin(MathHelper.sqrt_float(fSwing) * (float) Math.PI);
                GL11.glTranslatef(
                    -fSinSqrt * 0.4F,
                    MathHelper.sin(MathHelper.sqrt_float(fSwing) * (float) Math.PI * 2.0F) * 0.2F,
                    -fSin * 0.2F);
            }

            // Enhanced visibility for axes during combat swing
            boolean isAxe = !mining && Config.enableNewSwingAnimation && combatbackport$isAxe(itemstack);
            float fSwing = entityclientplayermp.getSwingProgress(partialTicks);
            float f1_sqrt = MathHelper.sin(MathHelper.sqrt_float(fSwing) * (float) Math.PI);

            float axeX = isAxe ? -0.06F * f1_sqrt : 0.0F;
            float axeY = isAxe ? 0.12F * f1_sqrt : 0.0F;
            float axeZ = isAxe ? -0.22F * f1_sqrt : 0.0F;
            float axePitch = isAxe ? -68.0F : -80.0F;

            // Authentic 1.9+ transformFirstPersonItem
            GL11.glTranslatef(0.56F + axeX, -0.52F + (1.0F - f1) * -0.6F + axeY, -0.71999997F + axeZ);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);

            float f = MathHelper.sin(fSwing * fSwing * (float) Math.PI);

            GL11.glRotatef(f * -20.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(f1_sqrt * -20.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(f1_sqrt * axePitch, 1.0F, 0.0F, 0.0F);

            float fItemScale = 0.4F;
            GL11.glScalef(fItemScale, fItemScale, fItemScale);

            if (entityclientplayermp.getItemInUseCount() > 0) {
                EnumAction enumaction = itemstack.getItemUseAction();
                if (enumaction == EnumAction.block) {
                    GL11.glTranslatef(-0.5F, 0.2F, 0.0F);
                    GL11.glRotatef(30.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(-80.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(60.0F, 0.0F, 1.0F, 0.0F);
                } else if (enumaction == EnumAction.bow) {
                    GL11.glRotatef(-18.0F, 0.0F, 0.0F, 1.0F);
                    GL11.glRotatef(-12.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(-8.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glTranslatef(-0.9F, 0.2F, 0.0F);
                    float fBowTime = (float) itemstack.getMaxItemUseDuration()
                        - ((float) entityclientplayermp.getItemInUseCount() - partialTicks + 1.0F);
                    float fBowPull = fBowTime / 20.0F;
                    fBowPull = (fBowPull * fBowPull + fBowPull * 2.0F) / 3.0F;
                    if (fBowPull > 1.0F) fBowPull = 1.0F;
                    if (fBowPull > 0.1F) {
                        GL11.glTranslatef(
                            0.0F,
                            MathHelper.sin((fBowTime - 0.1F) * 1.3F) * 0.01F * (fBowPull - 0.1F),
                            0.0F);
                    }
                    GL11.glTranslatef(0.0F, 0.0F, fBowPull * 0.1F);
                    GL11.glRotatef(-335.0F, 0.0F, 0.0F, 1.0F);
                    GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glTranslatef(0.0F, 0.5F, 0.0F);
                    float fBowScale = 1.0F + fBowPull * 0.2F;
                    GL11.glScalef(1.0F, 1.0F, fBowScale);
                    GL11.glTranslatef(0.0F, -0.5F, 0.0F);
                    GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
                }
            }

            if (itemstack.getItem()
                .shouldRotateAroundWhenRendering()) {
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            }

            if (itemstack.getItem()
                .requiresMultipleRenderPasses()) {
                this.renderItem(entityclientplayermp, itemstack, 0);
                int passes = itemstack.getItem()
                    .getRenderPasses(itemstack.getItemDamage());
                for (int p = 1; p < passes; ++p) {
                    int col = itemstack.getItem()
                        .getColorFromItemStack(itemstack, p);
                    float r = (float) (col >> 16 & 255) / 255.0F;
                    float g = (float) (col >> 8 & 255) / 255.0F;
                    float b = (float) (col & 255) / 255.0F;
                    GL11.glColor4f(1.0F * r, 1.0F * g, 1.0F * b, 1.0F);
                    this.renderItem(entityclientplayermp, itemstack, p);
                }
            } else {
                this.renderItem(entityclientplayermp, itemstack, 0);
            }

            GL11.glPopMatrix();
        } else if (!entityclientplayermp.isInvisible()) {
            GL11.glPushMatrix();

            if (!Config.enableNewSwingAnimation || mining) {
                float fSwing = entityclientplayermp.getSwingProgress(partialTicks);
                float fSin = MathHelper.sin(fSwing * (float) Math.PI);
                float fSinSqrt = MathHelper.sin(MathHelper.sqrt_float(fSwing) * (float) Math.PI);
                GL11.glTranslatef(
                    -fSinSqrt * 0.3F,
                    MathHelper.sin(MathHelper.sqrt_float(fSwing) * (float) Math.PI * 2.0F) * 0.4F,
                    -fSin * 0.4F);
            }

            GL11.glTranslatef(0.64F, -0.60F + (1.0F - f1) * -0.6F, -0.71999997F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);

            float fSwing = entityclientplayermp.getSwingProgress(partialTicks);
            float f = MathHelper.sin(fSwing * fSwing * (float) Math.PI);
            float f1_sqrt = MathHelper.sin(MathHelper.sqrt_float(fSwing) * (float) Math.PI);

            GL11.glRotatef(f * -20.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(f1_sqrt * -20.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(f1_sqrt * -80.0F, 1.0F, 0.0F, 0.0F);

            float fArmScale = 0.4F;
            GL11.glScalef(fArmScale, fArmScale, fArmScale);
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-1.0F, 3.6F, 3.5F);
            GL11.glScalef(1.0F, 1.0F, 1.0F);
            GL11.glTranslatef(5.6F, 0.0F, 0.0F);
            Render render = RenderManager.instance.getEntityRenderObject(this.mc.thePlayer);
            RenderPlayer renderPlayer = (RenderPlayer) render;
            renderPlayer.renderFirstPersonArm(this.mc.thePlayer);
            GL11.glPopMatrix();
        }

        if (itemstack != null && itemstack.getItem() instanceof ItemCloth) {
            GL11.glDisable(GL11.GL_BLEND);
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
    }
}
