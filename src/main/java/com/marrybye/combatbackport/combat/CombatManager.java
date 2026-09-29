package com.marrybye.combatbackport.combat;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import com.marrybye.combatbackport.CombatBackport;
import com.marrybye.combatbackport.Config;
import com.marrybye.combatbackport.api.ICombatPlayer;
import com.marrybye.combatbackport.combat.item.ItemShield;
import com.marrybye.combatbackport.network.CombatPacketHandler;
import com.marrybye.combatbackport.network.PacketShieldCooldown;
import com.marrybye.combatbackport.network.PacketSweepAttack;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public class CombatManager {

    public static final CombatManager INSTANCE = new CombatManager();

    /**
     * Executes the modern sweep attack around the primary target.
     */
    public static void performSweepAttack(EntityPlayer player, Entity primaryTarget, ItemStack weapon) {
        if (!Config.enableSweepAttack || player.worldObj.isRemote) {
            return;
        }

        double reach = 1.0D;
        AxisAlignedBB box = primaryTarget.boundingBox.expand(reach, 0.25D, reach);
        List<EntityLivingBase> list = player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, box);

        float sweepDamage = 1.0F; // Standard vanilla Minecraft 1.9+ sweep attack base damage (1 point / 0.5 hearts)
        if (CombatBackport.sweepingEdgeEnchantment != null && weapon != null) {
            int sweepLevel = EnchantmentHelper
                .getEnchantmentLevel(CombatBackport.sweepingEdgeEnchantment.effectId, weapon);
            if (sweepLevel > 0) {
                // Modern 1.11.1+ Sweeping Edge ratio: level / (level + 1)
                float ratio = (float) sweepLevel / (float) (sweepLevel + 1);
                float baseDamage = (float) player.getEntityAttribute(SharedMonsterAttributes.attackDamage)
                    .getAttributeValue();
                sweepDamage += ratio * baseDamage;
            }
        }
        int fireAspect = EnchantmentHelper.getFireAspectModifier(player);

        for (EntityLivingBase living : list) {
            if (living == player || living == primaryTarget) {
                continue;
            }
            if (!living.isEntityAlive()) {
                continue;
            }

            // Do not hit tamed pets owned by the player
            if (living instanceof EntityTameable && ((EntityTameable) living).getOwner() == player) {
                continue;
            }

            // Do not hit teammates
            if (player.isOnSameTeam(living)) {
                continue;
            }

            // Distance check from player (within 3.0 blocks max: distSq < 9.0)
            if (player.getDistanceSqToEntity(living) >= 9.0D) {
                continue;
            }

            // Vanilla 1.9+ sweep knockback: knockBack(player, 0.4F, sin(yaw), -cos(yaw))
            living.knockBack(
                player,
                0.4F,
                (double) MathHelper.sin(player.rotationYaw * (float) Math.PI / 180.0F),
                (double) (-MathHelper.cos(player.rotationYaw * (float) Math.PI / 180.0F)));

            // Apply sweep damage
            living.attackEntityFrom(DamageSource.causePlayerDamage(player), sweepDamage);

            if (fireAspect > 0 && !living.isBurning()) {
                living.setFire(fireAspect * 4);
            }
        }

        // Play authentic sweep sound
        player.worldObj
            .playSoundEffect(player.posX, player.posY, player.posZ, "combatbackport:player.attack.sweep", 1.0F, 1.0F);

        // Broadcast sweep particle packet to tracking clients (spawnSweepParticles formula)
        double d0 = (double) (-MathHelper.sin(player.rotationYaw * (float) Math.PI / 180.0F));
        double d1 = (double) (MathHelper.cos(player.rotationYaw * (float) Math.PI / 180.0F));
        double px = player.posX + d0;
        double py = player.posY + (double) player.height * 0.5D;
        double pz = player.posZ + d1;

        CombatPacketHandler.INSTANCE.sendToAllAround(
            new PacketSweepAttack(px, py, pz, player.rotationYaw),
            new TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 64.0D));
    }

    /**
     * Calculates scaled damage based on attack cooldown charge.
     */
    public static float getScaledDamage(float baseDamage, float charge) {
        if (!Config.enableDamageScaling) {
            return baseDamage;
        }
        // Vanilla 1.9+ formula: baseDamage * (0.2 + charge^2 * 0.8)
        float factor = 0.2F + (charge * charge) * 0.8F;
        return baseDamage * factor;
    }

    /**
     * Calculates modern armor toughness for an entity.
     * Diamond armor pieces provide +2 toughness each (total 8 for full set).
     */
    public static float getArmorToughness(EntityLivingBase entity) {
        if (entity == null) {
            return 0.0F;
        }
        float toughness = 0.0F;
        for (int i = 1; i <= 4; i++) {
            ItemStack piece = entity.getEquipmentInSlot(i);
            if (piece != null && piece.getItem() instanceof ItemArmor) {
                ItemArmor armor = (ItemArmor) piece.getItem();
                ItemArmor.ArmorMaterial mat = armor.getArmorMaterial();
                if (mat != null && mat.name()
                    .toLowerCase()
                    .contains("netherite")) {
                    toughness += 3.0F;
                } else if (mat == ItemArmor.ArmorMaterial.DIAMOND
                    || (mat != null && mat.getDamageReductionAmount(armor.armorType)
                        >= ItemArmor.ArmorMaterial.DIAMOND.getDamageReductionAmount(armor.armorType))) {
                            toughness += 2.0F;
                        }
            }
        }
        return toughness;
    }

    /**
     * Retrieves the currently active shield being used to block, if any.
     */
    public static ItemStack getActiveShield(EntityPlayer player) {
        if (player == null || !player.isUsingItem()) {
            return null;
        }
        ItemStack itemInUse = player.getItemInUse();
        if (itemInUse != null && itemInUse.getItem() instanceof ItemShield) {
            return itemInUse;
        }
        return null;
    }

    /**
     * Checks if the incoming damage source can be blocked by the player's shield.
     */
    public static boolean canBlockDamage(EntityPlayer player, DamageSource source) {
        if (source.isUnblockable()) {
            return false;
        }
        if (source == DamageSource.inFire || source == DamageSource.onFire || source == DamageSource.lava) {
            return false;
        }

        Entity attacker = source.getSourceOfDamage() != null ? source.getSourceOfDamage() : source.getEntity();
        if (attacker != null) {
            Vec3 lookVec = player.getLookVec();
            double dx = attacker.posX - player.posX;
            double dz = attacker.posZ - player.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0.0D) {
                dx /= dist;
                dz /= dist;
                double dot = lookVec.xCoord * dx + lookVec.zCoord * dz;
                return dot > 0.0D;
            }
        }
        return true;
    }

    /**
     * Handles modern shield blocking in LivingAttackEvent.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingAttack(LivingAttackEvent event) {
        if (!Config.enableShield || !(event.entityLiving instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.entityLiving;
        ItemStack shield = getActiveShield(player);
        if (shield == null || !canBlockDamage(player, event.source)) {
            return;
        }

        // Prevent repeated attacks every tick when blocking
        if (player.hurtResistantTime > player.maxHurtResistantTime / 2.0F) {
            event.setCanceled(true);
            return;
        }

        // Damage shield item (1 point per blocked hit, or based on damage)
        int shieldDmg = Math.max(1, (int) event.ammount);
        shield.damageItem(shieldDmg, player);
        if (shield.stackSize <= 0) {
            player.renderBrokenItemStack(shield);
            player.clearItemInUse();
        }

        // Authentic sound effect for shield block
        player.worldObj.playSoundAtEntity(
            player,
            "combatbackport:item.shield.block",
            1.0F,
            0.8F + player.worldObj.rand.nextFloat() * 0.4F);

        // Set hurt resistance window (20 ticks = 1 second) to match vanilla 1.9+
        player.hurtResistantTime = player.maxHurtResistantTime;

        // Deflect projectile
        if (event.source.isProjectile() && event.source.getSourceOfDamage() != null) {
            Entity proj = event.source.getSourceOfDamage();
            proj.motionX *= -0.2D;
            proj.motionY *= -0.2D;
            proj.motionZ *= -0.2D;
        }

        // Check axe shield disabling
        Entity attacker = event.source.getEntity();
        if (attacker instanceof EntityLivingBase) {
            EntityLivingBase livingAttacker = (EntityLivingBase) attacker;
            ItemStack attackerWeapon = livingAttacker.getHeldItem();
            if (attackerWeapon != null && WeaponRegistry.isAxe(attackerWeapon)) {
                float chance = livingAttacker.isSprinting() ? 1.0F : 0.25F;
                int eff = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, attackerWeapon);
                chance += (float) eff * 0.05F;

                if (player.getRNG()
                    .nextFloat() < chance) {
                    disableShield(player, 100); // 5 seconds
                }
            }

            // Knock back the attacker away from the shield (1.9+ mechanic)
            livingAttacker
                .knockBack(player, 0.5F, player.posX - livingAttacker.posX, player.posZ - livingAttacker.posZ);
        }

        // Slight knockback to player from impact
        if (attacker != null) {
            double dx = attacker.posX - player.posX;
            double dz = attacker.posZ - player.posZ;
            player.knockBack(attacker, 0.1F, -dx, -dz);
        }

        // Completely negate the blocked damage
        event.setCanceled(true);
    }

    /**
     * Disables the shield for the specified duration in ticks (e.g. 100 ticks = 5s).
     */
    public static void disableShield(EntityPlayer player, int ticks) {
        if (player instanceof ICombatPlayer) {
            ((ICombatPlayer) player).setShieldCooldown(ticks);
        }
        player.clearItemInUse();
        player.worldObj.playSoundAtEntity(player, "random.break", 0.9F, 0.8F);
        player.worldObj.playSoundAtEntity(player, "random.anvil_land", 0.4F, 1.8F);

        if (!player.worldObj.isRemote && player instanceof EntityPlayerMP) {
            CombatPacketHandler.INSTANCE.sendTo(new PacketShieldCooldown(ticks), (EntityPlayerMP) player);
        }
    }

    /**
     * Applies Mending enchantment repair to equipped damaged items using picked up XP.
     * Returns remaining XP points.
     */
    public static int applyMending(EntityPlayer player, int xp) {
        if (xp <= 0 || !Config.enableMendingEnchantment) {
            return xp;
        }

        // If Et Futurum handles Mending via its own PlayerPickupXpEvent, defer to it
        if (com.marrybye.combatbackport.compat.EtFuturumCompat.isMendingEnabledInEtFuturum()) {
            return xp;
        }

        List<ItemStack> damagedMendingItems = new ArrayList<>();

        // Main hand
        ItemStack main = player.getCurrentEquippedItem();
        if (isDamagedWithMending(main)) {
            damagedMendingItems.add(main);
        }

        // Armor
        if (player.inventory.armorInventory != null) {
            for (ItemStack armor : player.inventory.armorInventory) {
                if (isDamagedWithMending(armor)) {
                    damagedMendingItems.add(armor);
                }
            }
        }

        // Offhand (Backhand mod support)
        if (Loader.isModLoaded("backhand")) {
            try {
                ItemStack offhand = xonin.backhand.api.core.BackhandUtils.getOffhandItem(player);
                if (isDamagedWithMending(offhand)) {
                    damagedMendingItems.add(offhand);
                }
            } catch (Throwable ignored) {}
        }

        if (damagedMendingItems.isEmpty()) {
            return xp;
        }

        ItemStack target = damagedMendingItems.get(
            player.getRNG()
                .nextInt(damagedMendingItems.size()));
        int damage = target.getItemDamage();
        int repairPoints = xp * 2;
        int repaired = Math.min(repairPoints, damage);

        target.setItemDamage(damage - repaired);
        int xpUsed = (repaired + 1) / 2;

        return Math.max(0, xp - xpUsed);
    }

    private static boolean isDamagedWithMending(ItemStack stack) {
        if (stack == null || !stack.isItemDamaged()) {
            return false;
        }
        int effectId = -1;
        if (CombatBackport.mendingEnchantment != null) {
            effectId = CombatBackport.mendingEnchantment.effectId;
        } else if (com.marrybye.combatbackport.compat.EtFuturumCompat.isMendingEnabledInEtFuturum()) {
            effectId = com.marrybye.combatbackport.compat.EtFuturumCompat.getEtFuturumMendingId();
        }
        if (effectId <= 0) {
            return false;
        }
        return EnchantmentHelper.getEnchantmentLevel(effectId, stack) > 0;
    }
}
