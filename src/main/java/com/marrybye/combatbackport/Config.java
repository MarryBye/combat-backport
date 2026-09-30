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

    // 1.9+ Rebalance Mechanics
    public static boolean enableWeaponDamageRebalance = true;
    public static boolean enableModernArmorSystem = true;
    public static boolean disableSwordBlocking = true;
    public static boolean enableModernSharpness = true;
    public static boolean enableModernProtection = true;
    public static boolean enableSweepingEdgeEnchantment = true;
    public static int sweepingEdgeEnchantmentId = 74;
    public static boolean enableShield = true;
    public static boolean enableShieldTuckAway = true;
    public static boolean enableMendingEnchantment = true;
    public static int mendingEnchantmentId = 75;
    public static boolean enableModernHungerRegen = true;
    public static int fastRegenTickInterval = 10;
    public static int normalRegenTickInterval = 80;
    public static int starveTickInterval = 80;
    public static float fastRegenMaxExhaustion = 6.0F;
    public static float normalRegenExhaustion = 6.0F;
    public static int fastRegenMinFood = 20;
    public static int normalRegenMinFood = 18;

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

    public static String[] customTwoHandedWeapons = new String[0];
    public static String[] customRangedWeapons = new String[0];

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

        enableWeaponDamageRebalance = configuration.getBoolean(
            "enableWeaponDamageRebalance",
            "combat",
            enableWeaponDamageRebalance,
            "Rebalance weapons and tools damage according to Minecraft 1.9+ stats.");

        enableModernArmorSystem = configuration.getBoolean(
            "enableModernArmorSystem",
            "combat",
            enableModernArmorSystem,
            "Enable modern 1.9+ armor calculation formula with damage diminishing returns and diamond armor toughness.");

        disableSwordBlocking = configuration.getBoolean(
            "disableSwordBlocking",
            "combat",
            disableSwordBlocking,
            "Disable sword blocking on right click (1.9+ combat style).");

        enableModernSharpness = configuration.getBoolean(
            "enableModernSharpness",
            "combat",
            enableModernSharpness,
            "Update Sharpness enchantment to 1.9+ (+1.0 at level 1, +0.5 per additional level).");

        enableModernProtection = configuration.getBoolean(
            "enableModernProtection",
            "combat",
            enableModernProtection,
            "Update Protection enchantment to 1.9+ linear EPF without RNG.");

        enableSweepingEdgeEnchantment = configuration.getBoolean(
            "enableSweepingEdgeEnchantment",
            "enchantments",
            enableSweepingEdgeEnchantment,
            "Enable Sweeping Edge enchantment (boosts sweep attack damage).");

        int maxEnchantId = 255;
        try {
            if (net.minecraft.enchantment.Enchantment.enchantmentsList != null) {
                maxEnchantId = Math.max(255, net.minecraft.enchantment.Enchantment.enchantmentsList.length - 1);
            }
        } catch (Throwable ignored) {}

        sweepingEdgeEnchantmentId = configuration.getInt(
            "sweepingEdgeEnchantmentId",
            "enchantments",
            sweepingEdgeEnchantmentId,
            0,
            maxEnchantId,
            "Enchantment ID for Sweeping Edge. If this ID is already occupied by another mod or invalid, Combat Backport will automatically reassign it to the next available free ID and notify in the in-game chat.");

        enableMendingEnchantment = configuration.getBoolean(
            "enableMendingEnchantment",
            "enchantments",
            enableMendingEnchantment,
            "Enable Mending enchantment (repairs damaged items when collecting XP).");

        mendingEnchantmentId = configuration.getInt(
            "mendingEnchantmentId",
            "enchantments",
            mendingEnchantmentId,
            0,
            maxEnchantId,
            "Enchantment ID for Mending. If this ID is already occupied by another mod or invalid, Combat Backport will automatically reassign it to the next available free ID and notify in the in-game chat.");

        enableShield = configuration.getBoolean(
            "enableShield",
            "combat",
            enableShield,
            "Enable modern 1.9+ shield item, blocking, axe disabling, and planks repair.");

        enableModernHungerRegen = configuration.getBoolean(
            "enableModernHungerRegen",
            "combat",
            enableModernHungerRegen,
            "Enable modern 1.9+ rapid health regeneration when saturation is high and hunger bar is full.");

        fastRegenTickInterval = configuration.getInt(
            "fastRegenTickInterval",
            "combat",
            fastRegenTickInterval,
            1,
            1000,
            "Tick interval for rapid saturated regeneration (mode 1). Vanilla 1.9+ is 10 ticks (0.5s).");

        normalRegenTickInterval = configuration.getInt(
            "normalRegenTickInterval",
            "combat",
            normalRegenTickInterval,
            1,
            1000,
            "Tick interval for normal regeneration (mode 2). Vanilla 1.9+ is 80 ticks (4.0s).");

        starveTickInterval = configuration.getInt(
            "starveTickInterval",
            "combat",
            starveTickInterval,
            1,
            1000,
            "Tick interval for starvation damage when hunger is empty. Vanilla is 80 ticks (4.0s).");

        fastRegenMaxExhaustion = configuration.getFloat(
            "fastRegenMaxExhaustion",
            "combat",
            fastRegenMaxExhaustion,
            0.1F,
            40.0F,
            "Exhaustion drained per full point of rapid regeneration. Vanilla 1.9+ is 6.0.");

        normalRegenExhaustion = configuration.getFloat(
            "normalRegenExhaustion",
            "combat",
            normalRegenExhaustion,
            0.1F,
            40.0F,
            "Exhaustion drained per 1 HP of normal regeneration. Vanilla 1.9+ is 6.0.");

        fastRegenMinFood = configuration.getInt(
            "fastRegenMinFood",
            "combat",
            fastRegenMinFood,
            1,
            20,
            "Minimum food level required for rapid saturated regeneration. Vanilla 1.9+ is 20.");

        normalRegenMinFood = configuration.getInt(
            "normalRegenMinFood",
            "combat",
            normalRegenMinFood,
            1,
            20,
            "Minimum food level required for normal regeneration. Vanilla 1.9+ is 18.");

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

        enableShieldTuckAway = configuration.getBoolean(
            "enableShieldTuckAway",
            "shield",
            enableShieldTuckAway,
            "Tuck away (lower down and disable) offhand shield when holding or charging two-handed or ranged weapons.");

        customTwoHandedWeapons = configuration.getStringList(
            "customTwoHandedWeapons",
            "shield",
            customTwoHandedWeapons,
            "Registry names of custom two-handed weapons in format 'modid:item_name'.");

        customRangedWeapons = configuration.getStringList(
            "customRangedWeapons",
            "shield",
            customRangedWeapons,
            "Registry names of custom ranged weapons in format 'modid:item_name'.");

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

        // Initialize two-handed and ranged compat lists
        com.marrybye.combatbackport.compat.TwoHandedCompat.init(customTwoHandedWeapons, customRangedWeapons);

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
