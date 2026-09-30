package com.marrybye.combatbackport.mixins.late.ticon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.marrybye.combatbackport.Config;
import com.marrybye.combatbackport.api.ICombatPlayer;
import com.marrybye.combatbackport.combat.CombatManager;
import com.marrybye.combatbackport.combat.WeaponRegistry;

import tconstruct.library.tools.AbilityHelper;
import tconstruct.library.tools.ToolCore;

@Mixin(value = AbilityHelper.class, remap = false)
public abstract class MixinAbilityHelper {

    @WrapOperation(
        method = "onLeftClickEntity(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/Entity;Ltconstruct/library/tools/ToolCore;I)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z",
            remap = true),
        remap = false)
    private static boolean combatbackport$scaleTiCDamage(Entity target, DamageSource source, float amount,
        Operation<Boolean> original) {
        if (Config.enableAttackCooldown && Config.enableDamageScaling
            && source != null
            && source.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) source.getEntity();
            if (player instanceof ICombatPlayer) {
                ICombatPlayer combatPlayer = (ICombatPlayer) player;
                float charge = combatPlayer.getCooledAttackStrength(0.5F);
                amount = CombatManager.getScaledDamage(amount, charge);
                combatPlayer.setAttackScalingHandled(true);
                try {
                    return original.call(target, source, amount);
                } finally {
                    combatPlayer.setAttackScalingHandled(false);
                }
            }
        }
        return original.call(target, source, amount);
    }

    @WrapOperation(
        method = "onLeftClickEntity(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/Entity;Ltconstruct/library/tools/ToolCore;I)Z",
        at = @At(
            value = "INVOKE",
            target = "Ltconstruct/library/tools/AbilityHelper;calcKnockback(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;Ltconstruct/library/tools/ToolCore;Lnet/minecraft/nbt/NBTTagCompound;I)F",
            remap = false),
        remap = false)
    private static float combatbackport$scaleTiCKnockback(Entity attacker, Entity target, ItemStack stack,
        ToolCore tool, NBTTagCompound tags, int slot, Operation<Float> original) {
        float knockback = original.call(attacker, target, stack, tool, tags, slot);
        if (!Config.enableAttackCooldown || !Config.enableKnockbackScaling || !(attacker instanceof EntityPlayer)) {
            return knockback;
        }
        EntityPlayer player = (EntityPlayer) attacker;
        if (player instanceof ICombatPlayer) {
            float charge = ((ICombatPlayer) player).getCooledAttackStrength(0.5F);
            boolean isFullyCharged = charge >= 0.848F;
            return isFullyCharged ? knockback : knockback * 0.1F;
        }
        return knockback;
    }

    @WrapOperation(
        method = "onLeftClickEntity(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/Entity;Ltconstruct/library/tools/ToolCore;I)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/EntityLivingBase;isPotionActive(Lnet/minecraft/potion/Potion;)Z",
            remap = true),
        remap = false)
    private static boolean combatbackport$modifyTiCBlindness(EntityLivingBase attacker, Potion potion,
        Operation<Boolean> original) {
        boolean active = original.call(attacker, potion);
        if (active) return true;
        if (potion == Potion.blindness && attacker instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) attacker;
            if (player instanceof ICombatPlayer) {
                float charge = ((ICombatPlayer) player).getCooledAttackStrength(0.5F);
                boolean isFullyCharged = !Config.enableAttackCooldown || charge >= 0.848F;
                if (!isFullyCharged || player.isSprinting()) {
                    return true; // pretend blindness is active to cancel critical hit
                }
            }
        }
        return false;
    }

    @Inject(
        method = "onLeftClickEntity(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/Entity;Ltconstruct/library/tools/ToolCore;I)Z",
        at = @At("RETURN"),
        remap = false)
    private static void combatbackport$afterTiCAttack(ItemStack stack, EntityLivingBase attacker, Entity target,
        ToolCore tool, int slot, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && attacker instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) attacker;
            if (player instanceof ICombatPlayer) {
                ICombatPlayer cp = (ICombatPlayer) player;
                float charge = cp.getCooledAttackStrength(0.5F);
                boolean isFullyCharged = !Config.enableAttackCooldown || charge >= 0.848F;

                // Perform modern sweep attack if weapon supports it
                if (Config.enableSweepAttack && isFullyCharged
                    && !player.isSprinting()
                    && player.onGround
                    && WeaponRegistry.canSweep(stack)) {
                    CombatManager.performSweepAttack(player, target, stack);
                }

                cp.resetAttackCooldown();
            }
        }
    }
}
