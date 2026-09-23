package com.marrybye.combatbackport.mixins;

import net.minecraft.client.multiplayer.PlayerControllerMP;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.marrybye.combatbackport.client.ClientMiningHandler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {

    @Inject(method = "clickBlock", at = @At("HEAD"))
    private void combatbackport$onClickBlock(int x, int y, int z, int side, CallbackInfo ci) {
        ClientMiningHandler.onBlockInteracted();
    }

    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"))
    private void combatbackport$onDamageBlock(int x, int y, int z, int side, CallbackInfo ci) {
        ClientMiningHandler.onBlockInteracted();
    }

    @Inject(method = "onPlayerDestroyBlock", at = @At("HEAD"))
    private void combatbackport$onDestroyBlock(int x, int y, int z, int side, CallbackInfoReturnable<Boolean> cir) {
        ClientMiningHandler.onBlockDestroyed();
    }
}
