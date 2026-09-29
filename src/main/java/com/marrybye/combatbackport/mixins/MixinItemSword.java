package com.marrybye.combatbackport.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.marrybye.combatbackport.Config;

@Mixin(ItemSword.class)
public abstract class MixinItemSword {

    @Inject(method = "getItemUseAction", at = @At("HEAD"), cancellable = true)
    private void combatbackport$getItemUseAction(ItemStack stack, CallbackInfoReturnable<EnumAction> cir) {
        if (Config.disableSwordBlocking) {
            cir.setReturnValue(EnumAction.none);
        }
    }

    @Inject(method = "getMaxItemUseDuration", at = @At("HEAD"), cancellable = true)
    private void combatbackport$getMaxItemUseDuration(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (Config.disableSwordBlocking) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "onItemRightClick", at = @At("HEAD"), cancellable = true)
    private void combatbackport$onItemRightClick(ItemStack stack, World world, EntityPlayer player,
        CallbackInfoReturnable<ItemStack> cir) {
        if (Config.disableSwordBlocking) {
            cir.setReturnValue(stack);
        }
    }
}
