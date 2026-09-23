package com.marrybye.combatbackport.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntitySweepFX extends EntityFX {

    private static final ResourceLocation[] SWEEP_TEXTURES = new ResourceLocation[] {
        new ResourceLocation("combatbackport", "textures/particle/sweep_0.png"),
        new ResourceLocation("combatbackport", "textures/particle/sweep_1.png"),
        new ResourceLocation("combatbackport", "textures/particle/sweep_2.png"),
        new ResourceLocation("combatbackport", "textures/particle/sweep_3.png"),
        new ResourceLocation("combatbackport", "textures/particle/sweep_4.png"),
        new ResourceLocation("combatbackport", "textures/particle/sweep_5.png"),
        new ResourceLocation("combatbackport", "textures/particle/sweep_6.png"),
        new ResourceLocation("combatbackport", "textures/particle/sweep_7.png") };

    private final float sweepYaw;
    private final float maxScale;

    public EntitySweepFX(World world, double x, double y, double z, float yaw) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sweepYaw = yaw;
        this.particleMaxAge = 4;
        this.maxScale = 1.4F;
        this.noClip = true;
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setDead();
        }
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public void renderParticle(Tessellator unused, float partialTicks, float rotX, float rotXZ, float rotZ, float rotYZ,
        float rotXY) {
        float progress = ((float) this.particleAge + partialTicks) / (float) this.particleMaxAge;
        if (progress > 1.0F) {
            progress = 1.0F;
        }

        int frame = (int) (progress * 7.99F);
        if (frame < 0) frame = 0;
        if (frame > 7) frame = 7;

        ResourceLocation currentTexture = SWEEP_TEXTURES[frame];

        float px = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
        float py = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
        float pz = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);

        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(currentTexture);

        GL11.glPushMatrix();
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glDisable(GL11.GL_CULL_FACE);

        GL11.glTranslatef(px, py, pz);
        GL11.glRotatef(-this.sweepYaw, 0.0F, 1.0F, 0.0F);
        GL11.glScalef(this.maxScale, this.maxScale, this.maxScale);

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.setColorRGBA_F(1.0F, 1.0F, 1.0F, 1.0F);
        tess.setBrightness(15728880);

        float half = 0.5F;
        tess.addVertexWithUV(-half, 0.0D, -half, 0.0D, 1.0D);
        tess.addVertexWithUV(half, 0.0D, -half, 1.0D, 1.0D);
        tess.addVertexWithUV(half, 0.0D, half, 1.0D, 0.0D);
        tess.addVertexWithUV(-half, 0.0D, half, 0.0D, 0.0D);
        tess.draw();

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
    }
}
