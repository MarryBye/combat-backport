package com.marrybye.combatbackport.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.world.EnumDifficulty;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Unique
    private int combatbackport$lastRegenMode = -1;

    @Inject(method = "onUpdate", at = @At("HEAD"), cancellable = true)
    private void combatbackport$modernHungerUpdate(EntityPlayer player, CallbackInfo ci) {
        if (!Config.enableModernHungerRegen) {
            return;
        }
        ci.cancel();

        if (player == null || player.worldObj == null || player.worldObj.isRemote) {
            return;
        }

        EnumDifficulty difficulty = player.worldObj.difficultySetting;
        this.prevFoodLevel = this.foodLevel;

        // Process accumulated exhaustion
        while (this.foodExhaustionLevel >= 4.0F) {
            this.foodExhaustionLevel -= 4.0F;
            if (this.foodSaturationLevel > 0.0F) {
                this.foodSaturationLevel = Math.max(this.foodSaturationLevel - 1.0F, 0.0F);
            } else if (difficulty != EnumDifficulty.PEACEFUL) {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }

        boolean naturalRegen = player.worldObj.getGameRules()
            .getGameRuleBooleanValue("naturalRegeneration");
        boolean canHeal = player.shouldHeal();

        // Determine current mode:
        // 1: Rapid saturated regen (foodLevel >= fastRegenMinFood, saturation > 0, canHeal)
        // 2: Normal regen (foodLevel >= normalRegenMinFood, canHeal)
        // 3: Starvation (foodLevel <= 0)
        // 0: Idle / none
        int currentMode;
        if (naturalRegen && this.foodSaturationLevel > 0.0F && canHeal && this.foodLevel >= Config.fastRegenMinFood) {
            currentMode = 1;
        } else if (naturalRegen && this.foodLevel >= Config.normalRegenMinFood && canHeal) {
            currentMode = 2;
        } else if (this.foodLevel <= 0) {
            currentMode = 3;
        } else {
            currentMode = 0;
        }

        // Shared tickTimer is reset to 0 whenever mode transitions
        if (this.combatbackport$lastRegenMode == -1) {
            this.combatbackport$lastRegenMode = currentMode;
        } else if (currentMode != this.combatbackport$lastRegenMode) {
            this.foodTimer = 0;
            this.combatbackport$lastRegenMode = currentMode;
        }

        if (currentMode == 1) {
            // Mode 1: Rapid saturated regeneration
            ++this.foodTimer;
            if (this.foodTimer >= Config.fastRegenTickInterval) {
                float maxExhaustion = Math.max(Config.fastRegenMaxExhaustion, 0.1F);
                float f = Math.min(this.foodSaturationLevel, maxExhaustion);
                player.heal(f / maxExhaustion);
                this.addExhaustion(f);
                this.foodTimer = 0;
            }
        } else if (currentMode == 2) {
            // Mode 2: Normal regeneration
            ++this.foodTimer;
            if (this.foodTimer >= Config.normalRegenTickInterval) {
                player.heal(1.0F);
                this.addExhaustion(Config.normalRegenExhaustion);
                this.foodTimer = 0;
            }
        } else if (currentMode == 3) {
            // Mode 3: Starvation
            ++this.foodTimer;
            if (this.foodTimer >= Config.starveTickInterval) {
                if (player.isEntityAlive() && (player.getHealth() > 10.0F || difficulty == EnumDifficulty.HARD
                    || (player.getHealth() > 1.0F && difficulty == EnumDifficulty.NORMAL))) {
                    player.attackEntityFrom(DamageSource.starve, 1.0F);
                }
                this.foodTimer = 0;
            }
        } else {
            // Mode 0: Neither regen nor starving, timer remains 0
            this.foodTimer = 0;
        }
    }
}
