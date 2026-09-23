package com.marrybye.combatbackport.combat;

import java.util.List;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;

import com.marrybye.combatbackport.Config;
import com.marrybye.combatbackport.network.CombatPacketHandler;
import com.marrybye.combatbackport.network.PacketSweepAttack;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public class CombatManager {

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
}
