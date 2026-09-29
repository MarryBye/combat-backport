package com.marrybye.combatbackport.mixins;

import net.minecraft.enchantment.EnchantmentDamage;
import net.minecraft.entity.EnumCreatureAttribute;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.marrybye.combatbackport.Config;

@Mixin(EnchantmentDamage.class)
public abstract class MixinEnchantmentDamage {

    @Shadow
    @Final
    public int damageType;

    @Inject(method = "func_152376_a", at = @At("HEAD"), cancellable = true)
    private void combatbackport$calcModifierLiving(int level, EnumCreatureAttribute creatureType,
        CallbackInfoReturnable<Float> cir) {
        if (Config.enableModernSharpness && this.damageType == 0) {
            // Modern 1.9+ Sharpness: 1.0 base at level I, +0.5 per additional level
            cir.setReturnValue(level > 0 ? (1.0F + (float) (level - 1) * 0.5F) : 0.0F);
        }
    }
}
