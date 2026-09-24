package com.marrybye.combatbackport.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.marrybye.combatbackport.CombatBackport;
import com.marrybye.combatbackport.Config;
import com.marrybye.combatbackport.api.ICombatPlayer;
import com.marrybye.combatbackport.combat.CombatManager;
import com.marrybye.combatbackport.combat.WeaponRegistry;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase implements ICombatPlayer {

    @Shadow
    public InventoryPlayer inventory;

    @Shadow
    public abstract ItemStack getCurrentEquippedItem();

    @Unique
    private int combatbackport$ticksSinceLastSwing = 1000;

    @Unique
    private int combatbackport$lastSelectedSlot = -1;

    public MixinEntityPlayer(World world) {
        super(world);
    }

    @Unique
    private void combatbackport$checkSlotChange() {
        if (this.inventory == null) {
            return;
        }
        if (this.combatbackport$lastSelectedSlot == -1) {
            this.combatbackport$lastSelectedSlot = this.inventory.currentItem;
            return;
        }
        if (Config.enableItemSwitchCooldown) {
            if (this.combatbackport$lastSelectedSlot != this.inventory.currentItem) {
                this.combatbackport$lastSelectedSlot = this.inventory.currentItem;
                this.combatbackport$ticksSinceLastSwing = 0;
            }
        }
    }

    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void combatbackport$onPlayerUpdate(CallbackInfo ci) {
        this.combatbackport$ticksSinceLastSwing++;
        this.combatbackport$checkSlotChange();
    }

    @Override
    public float getCooledAttackStrength(float adjustTicks) {
        if (!Config.enableAttackCooldown) {
            return 1.0F;
        }
        float period = getAttackCooldownPeriod();
        float charge = ((float) this.combatbackport$ticksSinceLastSwing + adjustTicks) / period;
        return MathHelper.clamp_float(charge, 0.0F, 1.0F);
    }

    @Override
    public void resetAttackCooldown() {
        this.combatbackport$ticksSinceLastSwing = 0;
    }

    @Override
    public int getTicksSinceLastSwing() {
        return this.combatbackport$ticksSinceLastSwing;
    }

    @Override
    public void setTicksSinceLastSwing(int ticks) {
        this.combatbackport$ticksSinceLastSwing = ticks;
    }

    @Override
    public float getAttackCooldownPeriod() {
        float speed = WeaponRegistry.getAttackSpeed(this.getCurrentEquippedItem());

        // Apply Haste (+10% per level) and Mining Fatigue (-10% per level)
        float speedMultiplier = 1.0F;
        if (this.isPotionActive(Potion.digSpeed)) {
            PotionEffect haste = this.getActivePotionEffect(Potion.digSpeed);
            if (haste != null) {
                speedMultiplier += 0.1F * (float) (haste.getAmplifier() + 1);
            }
        }
        if (this.isPotionActive(Potion.digSlowdown)) {
            PotionEffect fatigue = this.getActivePotionEffect(Potion.digSlowdown);
            if (fatigue != null) {
                speedMultiplier -= 0.1F * (float) (fatigue.getAmplifier() + 1);
            }
        }
        if (speedMultiplier < 0.1F) {
            speedMultiplier = 0.1F;
        }
        speed *= speedMultiplier;

        if (speed <= 0.1F) {
            speed = 0.1F;
        }
        return 20.0F / speed;
    }

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At("HEAD"))
    private void combatbackport$beforeAttack(Entity targetEntity, CallbackInfo ci) {
        this.combatbackport$checkSlotChange();
    }

    @WrapOperation(
        method = "attackTargetEntityWithCurrentItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/attributes/IAttributeInstance;getAttributeValue()D"))
    private double combatbackport$scaleAttackDamage(IAttributeInstance instance, Operation<Double> original) {
        double baseDamage = original.call(instance);
        if (!Config.enableAttackCooldown || !Config.enableDamageScaling) {
            return baseDamage;
        }
        float charge = this.getCooledAttackStrength(0.5F);
        return (double) CombatManager.getScaledDamage((float) baseDamage, charge);
    }

    @WrapOperation(
        method = "attackTargetEntityWithCurrentItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/enchantment/EnchantmentHelper;getEnchantmentModifierLiving(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/EntityLivingBase;)F"))
    private float combatbackport$scaleEnchantModifier(EntityLivingBase attacker, EntityLivingBase target,
        Operation<Float> original) {
        float modifier = original.call(attacker, target);
        if (!Config.enableAttackCooldown || !Config.enableDamageScaling) {
            return modifier;
        }
        float charge = this.getCooledAttackStrength(0.5F);
        return modifier * charge;
    }

    @WrapOperation(
        method = "attackTargetEntityWithCurrentItem",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isSprinting()Z"))
    private boolean combatbackport$modifySprintKnockback(EntityPlayer player, Operation<Boolean> original) {
        boolean sprinting = original.call(player);
        if (!sprinting) {
            return false;
        }
        float charge = this.getCooledAttackStrength(0.5F);
        boolean isFullyCharged = !Config.enableAttackCooldown || charge >= 0.848F;
        return isFullyCharged;
    }

    @WrapOperation(
        method = "attackTargetEntityWithCurrentItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;isPotionActive(Lnet/minecraft/potion/Potion;)Z"))
    private boolean combatbackport$modifyBlindnessCheck(EntityPlayer player, Potion potion,
        Operation<Boolean> original) {
        boolean active = original.call(player, potion);
        if (active) {
            return true;
        }
        if (potion == Potion.blindness) {
            float charge = this.getCooledAttackStrength(0.5F);
            boolean isFullyCharged = !Config.enableAttackCooldown || charge >= 0.848F;
            if (!isFullyCharged || this.isSprinting()) {
                return true; // pretend blindness is active to cancel critical hit
            }
        }
        return false;
    }

    @WrapOperation(
        method = "attackTargetEntityWithCurrentItem",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    private void combatbackport$scaleKnockback(Entity target, double x, double y, double z, Operation<Void> original) {
        float charge = this.getCooledAttackStrength(0.5F);
        boolean isFullyCharged = !Config.enableAttackCooldown || charge >= 0.848F;
        float knockbackFactor = (Config.enableAttackCooldown && Config.enableKnockbackScaling && !isFullyCharged) ? 0.1F
            : 1.0F;
        original.call(target, x * (double) knockbackFactor, y * (double) knockbackFactor, z * (double) knockbackFactor);
    }

    @WrapOperation(
        method = "attackTargetEntityWithCurrentItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    private boolean combatbackport$handleAttackSuccess(Entity target, DamageSource source, float amount,
        Operation<Boolean> original) {
        float charge = this.getCooledAttackStrength(0.5F);
        boolean isFullyCharged = !Config.enableAttackCooldown || charge >= 0.848F;
        boolean isCritical = this.fallDistance > 0.0F && !this.onGround
            && !this.isOnLadder()
            && !this.isInWater()
            && !this.isPotionActive(Potion.blindness)
            && this.ridingEntity == null
            && !this.isSprinting()
            && isFullyCharged
            && target instanceof EntityLivingBase;

        ItemStack stack = this.getCurrentEquippedItem();
        boolean canSweep = Config.enableSweepAttack && isFullyCharged
            && !isCritical
            && !this.isSprinting()
            && this.onGround
            && WeaponRegistry.canSweep(stack);

        CombatBackport.LOG.info(
            String.format(
                "[Combat] Attacker: %s -> Target: %s | Cooldown Charge: %.2f (FullyCharged: %b) | Dealt Damage: %.2f | Critical: %b | Sweep: %b | Item: %s",
                this.getCommandSenderName(),
                target != null ? target.getClass()
                    .getSimpleName() : "null",
                charge,
                isFullyCharged,
                amount,
                isCritical,
                canSweep,
                stack != null ? stack.getDisplayName() : "Empty Hand"));

        boolean success = original.call(target, source, amount);
        if (success && canSweep) {
            CombatManager.performSweepAttack((EntityPlayer) (Object) this, target, stack);
        }
        return success;
    }

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At("RETURN"))
    private void combatbackport$afterAttack(Entity targetEntity, CallbackInfo ci) {
        this.resetAttackCooldown();
    }
}
