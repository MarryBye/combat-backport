package com.marrybye.combatbackport.mixins;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Shadow
    public abstract void onCriticalHit(Entity entity);

    @Shadow
    public abstract void onEnchantmentCritical(Entity entity);

    @Shadow
    public abstract void triggerAchievement(StatBase stat);

    @Shadow
    public abstract void destroyCurrentEquippedItem();

    @Shadow
    public abstract void addExhaustion(float amount);

    @Shadow
    public abstract void addStat(StatBase stat, int amount);

    @Unique
    private int combatbackport$ticksSinceLastSwing;

    @Unique
    private int combatbackport$lastSelectedSlot = -1;

    public MixinEntityPlayer(World world) {
        super(world);
    }

    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void combatbackport$onPlayerUpdate(CallbackInfo ci) {
        this.combatbackport$ticksSinceLastSwing++;

        // Detect hotbar item switch
        if (Config.enableItemSwitchCooldown && this.inventory != null) {
            if (this.combatbackport$lastSelectedSlot != this.inventory.currentItem) {
                this.combatbackport$lastSelectedSlot = this.inventory.currentItem;
                this.combatbackport$ticksSinceLastSwing = 0;
            }
        }
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
        if (speed <= 0.0F) {
            speed = WeaponRegistry.BASE_ATTACK_SPEED;
        }
        return 20.0F / speed;
    }

    /**
     * @author MarryBye & Gemini AI
     * @reason Implement modern Minecraft 1.9+ combat cooldown, damage scaling, knockback scaling, and sweep attacks.
     */
    @Overwrite
    public void attackTargetEntityWithCurrentItem(Entity targetEntity) {
        EntityPlayer self = (EntityPlayer) (Object) this;

        if (MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(self, targetEntity))) {
            return;
        }

        ItemStack stack = getCurrentEquippedItem();
        if (stack != null && stack.getItem()
            .onLeftClickEntity(stack, self, targetEntity)) {
            return;
        }

        if (targetEntity.canAttackWithItem()) {
            if (!targetEntity.hitByEntity(self)) {
                float f = (float) this.getEntityAttribute(SharedMonsterAttributes.attackDamage)
                    .getAttributeValue();
                int i = 0;
                float f1 = 0.0F;

                if (targetEntity instanceof EntityLivingBase) {
                    f1 = EnchantmentHelper.getEnchantmentModifierLiving(self, (EntityLivingBase) targetEntity);
                    i += EnchantmentHelper.getKnockbackModifier(self, (EntityLivingBase) targetEntity);
                }

                if (this.isSprinting()) {
                    ++i;
                }

                float charge = this.getCooledAttackStrength(0.5F);
                float scaledBaseDamage = f;
                float scaledEnchantDamage = f1;

                if (Config.enableAttackCooldown && Config.enableDamageScaling) {
                    scaledBaseDamage = CombatManager.getScaledDamage(f, charge);
                    scaledEnchantDamage = f1 * charge;
                }

                if (scaledBaseDamage > 0.0F || scaledEnchantDamage > 0.0F) {
                    boolean flag = this.fallDistance > 0.0F && !this.onGround
                        && !this.isOnLadder()
                        && !this.isInWater()
                        && !this.isPotionActive(Potion.blindness)
                        && this.ridingEntity == null
                        && targetEntity instanceof EntityLivingBase;

                    // Suppress critical hits if attack cooldown is not charged
                    if (Config.enableAttackCooldown && charge < 0.9F) {
                        flag = false;
                    }

                    if (flag && scaledBaseDamage > 0.0F) {
                        scaledBaseDamage *= 1.5F;
                    }

                    scaledBaseDamage += scaledEnchantDamage;
                    boolean flag1 = false;
                    int j = EnchantmentHelper.getFireAspectModifier(self);

                    if (targetEntity instanceof EntityLivingBase && j > 0 && !targetEntity.isBurning()) {
                        flag1 = true;
                        targetEntity.setFire(1);
                    }

                    boolean flag2 = targetEntity
                        .attackEntityFrom(DamageSource.causePlayerDamage(self), scaledBaseDamage);

                    if (flag2) {
                        if (i > 0) {
                            float knockbackFactor = (Config.enableAttackCooldown && Config.enableKnockbackScaling
                                && charge < 0.9F) ? 0.1F : 1.0F;
                            targetEntity.addVelocity(
                                (double) (-MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F) * (float) i
                                    * 0.5F
                                    * knockbackFactor),
                                0.1D * (double) knockbackFactor,
                                (double) (MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F) * (float) i
                                    * 0.5F
                                    * knockbackFactor));
                            this.motionX *= 0.6D;
                            this.motionZ *= 0.6D;
                            this.setSprinting(false);
                        }

                        // Modern sweep attack: charge > 0.9, onGround, not sprinting, not critical, sweeping weapon
                        if (Config.enableSweepAttack && charge > 0.9F
                            && !flag
                            && !this.isSprinting()
                            && this.onGround
                            && WeaponRegistry.canSweep(stack)) {
                            CombatManager.performSweepAttack(self, targetEntity, stack);
                        }

                        if (flag) {
                            this.onCriticalHit(targetEntity);
                        }

                        if (f1 > 0.0F) {
                            this.onEnchantmentCritical(targetEntity);
                        }

                        if (scaledBaseDamage >= 18.0F) {
                            this.triggerAchievement(AchievementList.overkill);
                        }

                        this.setLastAttacker(targetEntity);

                        if (targetEntity instanceof EntityLivingBase) {
                            EnchantmentHelper.func_151384_a((EntityLivingBase) targetEntity, self);
                        }

                        EnchantmentHelper.func_151385_b(self, targetEntity);
                        ItemStack itemstack = this.getCurrentEquippedItem();
                        Object object = targetEntity;

                        if (targetEntity instanceof EntityDragonPart) {
                            IEntityMultiPart ientitymultipart = ((EntityDragonPart) targetEntity).entityDragonObj;

                            if (ientitymultipart != null && ientitymultipart instanceof EntityLivingBase) {
                                object = (EntityLivingBase) ientitymultipart;
                            }
                        }

                        if (itemstack != null && object instanceof EntityLivingBase) {
                            itemstack.hitEntity((EntityLivingBase) object, self);

                            if (itemstack.stackSize <= 0) {
                                this.destroyCurrentEquippedItem();
                            }
                        }

                        if (targetEntity instanceof EntityLivingBase) {
                            this.addStat(StatList.damageDealtStat, Math.round(scaledBaseDamage * 10.0F));

                            if (j > 0) {
                                targetEntity.setFire(j * 4);
                            }
                        }

                        this.addExhaustion(0.3F);
                    } else if (flag1) {
                        targetEntity.extinguish();
                    }
                }

                // Reset attack cooldown after attack
                this.resetAttackCooldown();
            }
        }
    }
}
