package com.marrybye.combatbackport;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import com.marrybye.combatbackport.combat.AttackIndicatorMode;
import com.marrybye.combatbackport.combat.WeaponRegistry;

public class Config {

    public static Configuration configuration;

    // HUD / Client
    public static AttackIndicatorMode attackIndicatorMode = AttackIndicatorMode.CROSSHAIR;

    // Combat Mechanics
    public static boolean enableAttackCooldown = true;
    public static boolean enableSweepAttack = true;
    public static boolean enableDamageScaling = true;
    public static boolean enableKnockbackScaling = true;
    public static boolean enableAirSwingCooldown = true;
    public static boolean enableItemSwitchCooldown = true;
    public static boolean enableItemReequipAnimation = true;

    // Custom items configuration
    public static String[] customWeaponSpeeds = new String[] {
        // "minecraft:diamond_sword=1.6",
    };

    public static String[] customWeaponDamages = new String[] {
        // "minecraft:diamond_axe=9.0",
    };

    public static String[] customSweepItems = new String[] {
        // "minecraft:iron_sword",
    };

    public static void synchronizeConfiguration(File configFile) {
        if (configuration == null) {
            configuration = new Configuration(configFile);
        }

        attackIndicatorMode = AttackIndicatorMode.fromString(
            configuration.getString(
                "attackIndicatorMode",
                "client",
                attackIndicatorMode.name(),
                "Attack indicator position. Options: CROSSHAIR, HOTBAR, DISABLED"));

        enableAttackCooldown = configuration.getBoolean(
            "enableAttackCooldown",
            "combat",
            enableAttackCooldown,
            "Enable modern attack cooldown mechanic.");

        enableSweepAttack = configuration.getBoolean(
            "enableSweepAttack",
            "combat",
            enableSweepAttack,
            "Enable modern sweep attack (AoE damage and knockback on fully-charged sword strikes).");

        enableDamageScaling = configuration.getBoolean(
            "enableDamageScaling",
            "combat",
            enableDamageScaling,
            "Scale damage based on attack cooldown readiness (Damage * (0.2 + charge^2 * 0.8)).");

        enableKnockbackScaling = configuration.getBoolean(
            "enableKnockbackScaling",
            "combat",
            enableKnockbackScaling,
            "Suppress or scale knockback when attack is not fully charged.");

        enableAirSwingCooldown = configuration.getBoolean(
            "enableAirSwingCooldown",
            "combat",
            enableAirSwingCooldown,
            "Reset attack cooldown when swinging at empty air (missed swing).");

        enableItemSwitchCooldown = configuration.getBoolean(
            "enableItemSwitchCooldown",
            "combat",
            enableItemSwitchCooldown,
            "Reset attack cooldown when switching active hotbar item.");

        enableItemReequipAnimation = configuration.getBoolean(
            "enableItemReequipAnimation",
            "client",
            enableItemReequipAnimation,
            "Enable modern 1.9+ item re-equip rising animation based on attack cooldown.");

        customWeaponSpeeds = configuration.getStringList(
            "customWeaponSpeeds",
            "weapons",
            customWeaponSpeeds,
            "Custom weapon attack speeds in format 'modid:item_name=speed'.");

        customWeaponDamages = configuration.getStringList(
            "customWeaponDamages",
            "weapons",
            customWeaponDamages,
            "Custom weapon base damages in format 'modid:item_name=damage'.");

        customSweepItems = configuration.getStringList(
            "customSweepItems",
            "weapons",
            customSweepItems,
            "Items forced to enable sweep attacks in format 'modid:item_name'.");

        // Parse custom weapon speeds
        WeaponRegistry.clearCache();
        WeaponRegistry.CUSTOM_SPEEDS.clear();
        for (String entry : customWeaponSpeeds) {
            String[] parts = entry.split("=");
            if (parts.length == 2) {
                try {
                    WeaponRegistry.CUSTOM_SPEEDS.put(parts[0].trim(), Float.parseFloat(parts[1].trim()));
                } catch (NumberFormatException ignored) {}
            }
        }

        // Parse custom damages
        WeaponRegistry.CUSTOM_DAMAGES.clear();
        for (String entry : customWeaponDamages) {
            String[] parts = entry.split("=");
            if (parts.length == 2) {
                try {
                    WeaponRegistry.CUSTOM_DAMAGES.put(parts[0].trim(), Float.parseFloat(parts[1].trim()));
                } catch (NumberFormatException ignored) {}
            }
        }

        // Parse custom sweep items
        WeaponRegistry.CUSTOM_SWEEP_ITEMS.clear();
        for (String entry : customSweepItems) {
            if (!entry.trim()
                .isEmpty()) {
                WeaponRegistry.CUSTOM_SWEEP_ITEMS.add(entry.trim());
            }
        }

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    public static void saveConfig() {
        if (configuration != null) {
            configuration.get("client", "attackIndicatorMode", attackIndicatorMode.name())
                .set(attackIndicatorMode.name());
            configuration.save();
        }
    }
}
