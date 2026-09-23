package com.marrybye.combatbackport.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.marrybye.combatbackport.Config;
import com.marrybye.combatbackport.api.ICombatPlayer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Shadow
    @Final
    private Minecraft mc;

    @Inject(
        method = "renderItemInFirstPerson",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItem(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;ILnet/minecraftforge/client/IItemRenderer$ItemRenderType;)V",
            remap = false))
    private void combatbackport$applyItemRechargeOffset(float partialTicks, CallbackInfo ci) {
        if (!Config.enableAttackCooldown) {
            return;
        }

        if (this.mc != null && this.mc.thePlayer instanceof ICombatPlayer) {
            ICombatPlayer player = (ICombatPlayer) this.mc.thePlayer;
            float charge = player.getCooledAttackStrength(partialTicks);
            if (charge < 1.0F) {
                float f = 1.0F - charge;
                // Vanilla 1.9+ cubic recharge curve: f^3 gives the snappy, authentic Minecraft rise
                float offset = f * f * f * 2.2F;
                GL11.glTranslatef(0.0F, -offset, 0.0F);
            }
        }
    }

    @Inject(
        method = "renderItemInFirstPerson",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/RenderPlayer;renderFirstPersonArm(Lnet/minecraft/entity/player/EntityPlayer;)V"))
    private void combatbackport$applyArmRechargeOffset(float partialTicks, CallbackInfo ci) {
        if (!Config.enableAttackCooldown) {
            return;
        }

        if (this.mc != null && this.mc.thePlayer instanceof ICombatPlayer) {
            ICombatPlayer player = (ICombatPlayer) this.mc.thePlayer;
            float charge = player.getCooledAttackStrength(partialTicks);
            if (charge < 1.0F) {
                float f = 1.0F - charge;
                float offset = f * f * f * 0.9F;
                GL11.glTranslatef(0.0F, -offset, 0.0F);
            }
        }
    }
}
