package com.marrybye.combatbackport.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.marrybye.combatbackport.Config;
import com.marrybye.combatbackport.combat.CombatManager;

@Mixin(EntityXPOrb.class)
public abstract class MixinEntityXPOrb extends Entity {

    public MixinEntityXPOrb(World world) {
        super(world);
    }

    @WrapOperation(
        method = "onCollideWithPlayer",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;addExperience(I)V"))
    private void combatbackport$wrapAddExperience(EntityPlayer player, int amount, Operation<Void> original) {
        if (!this.worldObj.isRemote && Config.enableMendingEnchantment && amount > 0) {
            amount = CombatManager.applyMending(player, amount);
        }
        if (amount > 0) {
            original.call(player, amount);
        }
    }
}
