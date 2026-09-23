package com.marrybye.combatbackport.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.util.MovingObjectPosition;

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
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    public EntityClientPlayerMP thePlayer;

    @Shadow
    public MovingObjectPosition objectMouseOver;

    @Inject(method = "func_147116_af", at = @At("HEAD"))
    private void combatbackport$onLeftClick(CallbackInfo ci) {
        if (!Config.enableAttackCooldown || this.thePlayer == null || !(this.thePlayer instanceof ICombatPlayer)) {
            return;
        }

        // Only reset cooldown on missed swing (air) or when attacking an entity.
        // Never reset cooldown when mining or interacting with blocks.
        if (this.objectMouseOver == null
            || this.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) {
            if (Config.enableAirSwingCooldown) {
                ((ICombatPlayer) this.thePlayer).resetAttackCooldown();
            }
        } else if (this.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            ((ICombatPlayer) this.thePlayer).resetAttackCooldown();
        }
    }
}
