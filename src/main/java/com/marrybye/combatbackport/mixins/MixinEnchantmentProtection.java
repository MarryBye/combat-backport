package com.marrybye.combatbackport.mixins;

import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.util.DamageSource;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.marrybye.combatbackport.Config;

@Mixin(EnchantmentProtection.class)
public abstract class MixinEnchantmentProtection {

    @Shadow
    @Final
    public int protectionType;

    @Inject(method = "calcModifierDamage", at = @At("HEAD"), cancellable = true)
    private void combatbackport$calcModifierDamage(int level, DamageSource source,
        CallbackInfoReturnable<Integer> cir) {
        if (!Config.enableModernProtection) {
            return;
        }
        if (source.canHarmInCreative()) {
            cir.setReturnValue(0);
            return;
        }
        // Modern 1.9+ Linear EPF calculation
        if (this.protectionType == 0) {
            cir.setReturnValue(level); // 1 EPF per level
        } else if (this.protectionType == 1 && source.isFireDamage()) {
            cir.setReturnValue(level * 2); // 2 EPF per level
        } else if (this.protectionType == 2 && source == DamageSource.fall) {
            cir.setReturnValue(level * 3); // 3 EPF per level
        } else if (this.protectionType == 3 && source.isExplosion()) {
            cir.setReturnValue(level * 2); // 2 EPF per level
        } else if (this.protectionType == 4 && source.isProjectile()) {
            cir.setReturnValue(level * 2); // 2 EPF per level
        } else {
            cir.setReturnValue(0);
        }
    }
}
