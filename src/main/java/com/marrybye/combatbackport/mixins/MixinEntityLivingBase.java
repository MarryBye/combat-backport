package com.marrybye.combatbackport.mixins;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.marrybye.combatbackport.Config;
import com.marrybye.combatbackport.combat.CombatManager;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase {

    @Shadow
    public abstract int getTotalArmorValue();

    @Shadow
    protected abstract void damageArmor(float damage);

    @Inject(method = "applyArmorCalculations", at = @At("HEAD"), cancellable = true)
    private void combatbackport$applyArmorCalculations(DamageSource source, float damage,
        CallbackInfoReturnable<Float> cir) {
        if (!Config.enableModernArmorSystem) {
            return;
        }

        if (!source.isUnblockable()) {
            this.damageArmor(damage);
            float armor = (float) this.getTotalArmorValue();
            float toughness = CombatManager.getArmorToughness((EntityLivingBase) (Object) this);

            // Modern 1.9+ formula: damage * (1 - clamp(armor - damage / (2 + toughness / 4), armor * 0.2, 20) / 25)
            float f = 2.0F + toughness / 4.0F;
            float f1 = MathHelper.clamp_float(armor - damage / f, armor * 0.2F, 20.0F);
            cir.setReturnValue(damage * (1.0F - f1 / 25.0F));
        }
    }
}
