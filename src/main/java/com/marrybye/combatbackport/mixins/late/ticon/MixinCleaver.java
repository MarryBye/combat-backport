package com.marrybye.combatbackport.mixins.late.ticon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import tconstruct.items.tools.Cleaver;

@Mixin(value = Cleaver.class, remap = false)
public abstract class MixinCleaver {

    @Redirect(
        method = "onUpdate",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;addPotionEffect(Lnet/minecraft/potion/PotionEffect;)V",
            remap = true),
        remap = false)
    private void combatbackport$suppressCleaverFatigue(EntityPlayer player, PotionEffect effect) {
        if (effect != null && effect.getPotionID() == Potion.digSlowdown.id) {
            // Remove any existing Cleaver fatigue from player
            if (player.isPotionActive(Potion.digSlowdown)) {
                PotionEffect current = player.getActivePotionEffect(Potion.digSlowdown);
                if (current != null && current.getAmplifier() == 2) {
                    player.removePotionEffect(Potion.digSlowdown.id);
                }
            }
            return;
        }
        player.addPotionEffect(effect);
    }

    @Inject(method = "onUpdate", at = @At("HEAD"), remap = false)
    private void combatbackport$clearExistingCleaverFatigue(ItemStack stack, World world, Entity entity, int slot,
        boolean isCurrent, CallbackInfo ci) {
        if (isCurrent && entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            if (player.isPotionActive(Potion.digSlowdown)) {
                PotionEffect current = player.getActivePotionEffect(Potion.digSlowdown);
                if (current != null && current.getAmplifier() == 2 && current.getDuration() <= 20) {
                    player.removePotionEffect(Potion.digSlowdown.id);
                }
            }
        }
    }
}
