package com.marrybye.combatbackport.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.world.EnumDifficulty;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.marrybye.combatbackport.Config;

@Mixin(FoodStats.class)
public abstract class MixinFoodStats {

    @Shadow
    private int foodLevel;

    @Shadow
    private float foodSaturationLevel;

    @Shadow
    private float foodExhaustionLevel;

    @Shadow
    private int foodTimer;

    @Shadow
    private int prevFoodLevel;

    @Shadow
    public abstract void addExhaustion(float exhaustion);

    @Inject(method = "onUpdate", at = @At("HEAD"), cancellable = true)
    private void combatbackport$modernHungerUpdate(EntityPlayer player, CallbackInfo ci) {
        if (!Config.enableModernHungerRegen) {
            return;
        }
        ci.cancel();

        EnumDifficulty difficulty = player.worldObj.difficultySetting;
        this.prevFoodLevel = this.foodLevel;

        if (this.foodExhaustionLevel > 4.0F) {
            this.foodExhaustionLevel -= 4.0F;
            if (this.foodSaturationLevel > 0.0F) {
                this.foodSaturationLevel = Math.max(this.foodSaturationLevel - 1.0F, 0.0F);
            } else if (difficulty != EnumDifficulty.PEACEFUL) {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }

        boolean naturalRegen = player.worldObj.getGameRules()
            .getGameRuleBooleanValue("naturalRegeneration");

        // 1.9+ Saturated rapid regeneration: full food bar (20) and saturation > 0
        if (naturalRegen && this.foodSaturationLevel > 0.0F && player.shouldHeal() && this.foodLevel >= 20) {
            ++this.foodTimer;
            if (this.foodTimer >= 10) {
                player.heal(1.0F);
                this.addExhaustion(6.0F);
                this.foodTimer = 0;
            }
        } else if (naturalRegen && this.foodLevel >= 18 && player.shouldHeal()) {
            // 1.9+ Normal regeneration: food >= 18, heals 1 HP every 80 ticks, draining 6 exhaustion
            ++this.foodTimer;
            if (this.foodTimer >= 80) {
                player.heal(1.0F);
                this.addExhaustion(6.0F);
                this.foodTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
            ++this.foodTimer;
            if (this.foodTimer >= 80) {
                if (player.getHealth() > 10.0F || difficulty == EnumDifficulty.HARD
                    || (player.getHealth() > 1.0F && difficulty == EnumDifficulty.NORMAL)) {
                    player.attackEntityFrom(DamageSource.starve, 1.0F);
                }
                this.foodTimer = 0;
            }
        } else {
            this.foodTimer = 0;
        }
    }
}
