package com.marrybye.combatbackport.mixins.late.ticon;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.marrybye.combatbackport.Config;

import tconstruct.library.tools.Weapon;

@Mixin(value = Weapon.class, remap = false)
public abstract class MixinWeapon {

    @Inject(method = "getItemUseAction", at = @At("HEAD"), cancellable = true)
    private void combatbackport$getTiCItemUseAction(ItemStack stack, CallbackInfoReturnable<EnumAction> cir) {
        if (Config.disableSwordBlocking) {
            cir.setReturnValue(EnumAction.none);
        }
    }

    @Inject(method = "getMaxItemUseDuration", at = @At("HEAD"), cancellable = true)
    private void combatbackport$getTiCMaxItemUseDuration(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (Config.disableSwordBlocking && !"Longsword".equals(
            this.getClass()
                .getSimpleName())) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "onItemRightClick", at = @At("HEAD"), cancellable = true)
    private void combatbackport$onTiCItemRightClick(ItemStack stack, World world, EntityPlayer player,
        CallbackInfoReturnable<ItemStack> cir) {
        if (Config.disableSwordBlocking) {
            cir.setReturnValue(stack);
        }
    }
}
