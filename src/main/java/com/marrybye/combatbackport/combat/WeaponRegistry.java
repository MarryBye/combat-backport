package com.marrybye.combatbackport.combat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;

import com.google.common.collect.Multimap;

import cpw.mods.fml.common.registry.GameRegistry;

public class WeaponRegistry {

    public static final float BASE_ATTACK_SPEED = 4.0F; // Hand / generic speed (5 ticks cooldown = 0.25s)
    public static final UUID ITEM_MODIFIER_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");

    private static final Map<Item, Float> CACHED_SPEEDS = new HashMap<>();
    private static final Map<Item, Boolean> CACHED_SWEEP = new HashMap<>();

    public static final Map<String, Float> CUSTOM_SPEEDS = new HashMap<>();
    public static final Map<String, Float> CUSTOM_DAMAGES = new HashMap<>();
    public static final Set<String> CUSTOM_SWEEP_ITEMS = new HashSet<>();

    public static void clearCache() {
        CACHED_SPEEDS.clear();
        CACHED_SWEEP.clear();
    }

    /**
     * Gets the attack speed for the given item stack.
     * Attack speed represents attacks per second (e.g. 1.6 = 1.6 attacks/sec -> 12.5 ticks cooldown).
     */
    public static float getAttackSpeed(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return BASE_ATTACK_SPEED;
        }

        Item item = stack.getItem();

        // 1. Check custom configured speeds
        String regName = getItemRegistryName(item);
        if (regName != null && CUSTOM_SPEEDS.containsKey(regName)) {
            return CUSTOM_SPEEDS.get(regName);
        }

        // 2. Check cached speed
        Float cached = CACHED_SPEEDS.get(item);
        if (cached != null) {
            return cached;
        }

        float speed = calculateAttackSpeed(item, stack);
        CACHED_SPEEDS.put(item, speed);
        return speed;
    }

    private static float calculateAttackSpeed(Item item, ItemStack stack) {
        // Standard Vanilla Swords
        if (item instanceof ItemSword) {
            return 1.6F;
        }

        // Standard Vanilla & Modded Axes
        if (item instanceof ItemAxe) {
            ToolMaterial mat = getToolMaterial(item);
            if (mat == ToolMaterial.WOOD || mat == ToolMaterial.STONE) {
                return 0.8F;
            } else if (mat == ToolMaterial.IRON) {
                return 0.9F;
            } else if (mat == ToolMaterial.EMERALD || mat == ToolMaterial.GOLD
                || (mat != null && mat.name()
                    .toLowerCase()
                    .contains("netherite"))) {
                        return 1.0F;
                    }
            return 0.9F;
        }

        // Pickaxes
        if (item instanceof ItemPickaxe) {
            return 1.2F;
        }

        // Shovels
        if (item instanceof ItemSpade) {
            return 1.0F;
        }

        // Hoes
        if (item instanceof ItemHoe) {
            ToolMaterial mat = getToolMaterial(item);
            if (mat == ToolMaterial.WOOD || mat == ToolMaterial.STONE || mat == ToolMaterial.GOLD) {
                return 1.0F;
            } else if (mat == ToolMaterial.IRON) {
                return 2.0F;
            } else if (mat == ToolMaterial.EMERALD || (mat != null && mat.name()
                .toLowerCase()
                .contains("netherite"))) {
                    return 4.0F;
                }
            return 2.0F;
        }

        // Tinkers' Construct / Modded Tool Heuristics
        String className = item.getClass()
            .getName();
        String simpleName = item.getClass()
            .getSimpleName();

        // TiC detection
        if (className.contains("tconstruct") || className.contains("tinkers")) {
            if (simpleName.equalsIgnoreCase("Broadsword")) return 1.6F;
            if (simpleName.equalsIgnoreCase("Longsword")) return 1.4F;
            if (simpleName.equalsIgnoreCase("Rapier")) return 3.0F;
            if (simpleName.equalsIgnoreCase("Dagger") || simpleName.equalsIgnoreCase("Knife")) return 3.0F;
            if (simpleName.equalsIgnoreCase("Cutlass")) return 2.0F;
            if (simpleName.equalsIgnoreCase("Cleaver")) return 0.7F;
            if (simpleName.equalsIgnoreCase("Battleaxe")) return 0.8F;
            if (simpleName.equalsIgnoreCase("Scythe")) return 0.9F;
            if (simpleName.equalsIgnoreCase("LumberAxe") || simpleName.equalsIgnoreCase("Hammer")
                || simpleName.equalsIgnoreCase("Excavator")) return 0.6F;
            if (simpleName.equalsIgnoreCase("Hatchet") || simpleName.equalsIgnoreCase("Mattock")) return 1.0F;
            if (simpleName.equalsIgnoreCase("Pickaxe")) return 1.2F;
            if (simpleName.equalsIgnoreCase("Shovel")) return 1.0F;
            if (simpleName.equalsIgnoreCase("Frypan") || simpleName.equalsIgnoreCase("Battlesign")) return 1.2F;
        }

        // Tool classes check
        Set<String> toolClasses = item.getToolClasses(stack);
        if (toolClasses != null) {
            if (toolClasses.contains("sword")) return 1.6F;
            if (toolClasses.contains("axe")) return 0.9F;
            if (toolClasses.contains("pickaxe")) return 1.2F;
            if (toolClasses.contains("shovel") || toolClasses.contains("spade")) return 1.0F;
            if (toolClasses.contains("hoe")) return 2.0F;
        }

        // Generic Name Patterns for Modded Weapons
        String lowerName = simpleName.toLowerCase();
        if (lowerName.contains("rapier") || lowerName.contains("dagger")
            || lowerName.contains("knife")
            || lowerName.contains("stiletto")) {
            return 3.0F;
        }
        if (lowerName.contains("sword") || lowerName.contains("blade")
            || lowerName.contains("katana")
            || lowerName.contains("saber")) {
            return 1.6F;
        }
        if (lowerName.contains("claymore") || lowerName.contains("greatsword")
            || lowerName.contains("warhammer")
            || lowerName.contains("mace")
            || lowerName.contains("club")
            || lowerName.contains("cleaver")) {
            return 0.7F;
        }
        if (lowerName.contains("battleaxe") || lowerName.contains("halberd") || lowerName.contains("axe")) {
            return 0.9F;
        }
        if (lowerName.contains("spear") || lowerName.contains("lance")
            || lowerName.contains("pike")
            || lowerName.contains("trident")
            || lowerName.contains("glaive")) {
            return 1.1F;
        }
        if (lowerName.contains("scythe") || lowerName.contains("sickle")) {
            return 0.9F;
        }
        if (lowerName.contains("pickaxe")) {
            return 1.2F;
        }
        if (lowerName.contains("shovel") || lowerName.contains("spade")) {
            return 1.0F;
        }
        if (lowerName.contains("hoe")) {
            return 2.0F;
        }

        return BASE_ATTACK_SPEED;
    }

    /**
     * Determines whether the given item is an axe (vanilla or modded).
     */
    public static boolean isAxe(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }
        Item item = stack.getItem();
        if (item instanceof ItemAxe) {
            return true;
        }
        if (item.getToolClasses(stack) != null && item.getToolClasses(stack)
            .contains("axe")) {
            return true;
        }
        String name = item.getClass()
            .getSimpleName()
            .toLowerCase();
        return name.contains("axe") && !name.contains("pickaxe");
    }

    /**
     * Determines whether the given item can perform a sweep attack.
     */
    public static boolean canSweep(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }

        Item item = stack.getItem();

        String regName = getItemRegistryName(item);
        if (regName != null && CUSTOM_SWEEP_ITEMS.contains(regName)) {
            return true;
        }

        Boolean cached = CACHED_SWEEP.get(item);
        if (cached != null) {
            return cached;
        }

        boolean sweep = calculateCanSweep(item, stack);
        CACHED_SWEEP.put(item, sweep);
        return sweep;
    }

    private static boolean calculateCanSweep(Item item, ItemStack stack) {
        if (item instanceof ItemSword) {
            return true;
        }

        String className = item.getClass()
            .getName();
        String simpleName = item.getClass()
            .getSimpleName();

        // TiC items
        if (className.contains("tconstruct") || className.contains("tinkers")) {
            if (simpleName.equalsIgnoreCase("Broadsword") || simpleName.equalsIgnoreCase("Longsword")
                || simpleName.equalsIgnoreCase("Cutlass")
                || simpleName.equalsIgnoreCase("Cleaver")
                || simpleName.equalsIgnoreCase("Scythe")) {
                return true;
            }
            if (simpleName.equalsIgnoreCase("Rapier") || simpleName.equalsIgnoreCase("Dagger")) {
                return false;
            }
        }

        // Tool classes
        Set<String> toolClasses = item.getToolClasses(stack);
        if (toolClasses != null && toolClasses.contains("sword")) {
            return true;
        }

        // Name heuristics
        String lower = simpleName.toLowerCase();
        if (lower.contains("rapier") || lower.contains("dagger") || lower.contains("knife")) {
            return false;
        }
        if (lower.contains("sword") || lower.contains("blade")
            || lower.contains("katana")
            || lower.contains("claymore")
            || lower.contains("saber")
            || lower.contains("cutlass")
            || lower.contains("scythe")) {
            return true;
        }

        return false;
    }

    public static String getItemRegistryName(Item item) {
        try {
            GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(item);
            if (uid != null) {
                return uid.modId + ":" + uid.name;
            }
        } catch (Exception ignored) {}
        return Item.itemRegistry.getNameForObject(item);
    }

    public static boolean isWeaponOrTool(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }

        Item item = stack.getItem();

        // Standard tool / weapon classes
        if (item instanceof ItemSword || item instanceof ItemTool || item instanceof ItemHoe) {
            return true;
        }

        // Custom configured speeds or damages
        String regName = getItemRegistryName(item);
        if (regName != null && (CUSTOM_SPEEDS.containsKey(regName) || CUSTOM_DAMAGES.containsKey(regName))) {
            return true;
        }

        // Tool classes check
        Set<String> toolClasses = item.getToolClasses(stack);
        if (toolClasses != null && !toolClasses.isEmpty()) {
            return true;
        }

        // Check if item has attack damage attribute modifiers
        try {
            Multimap<String, AttributeModifier> modifiers = stack.getAttributeModifiers();
            if (modifiers != null
                && modifiers.containsKey(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName())) {
                return true;
            }
        } catch (Exception ignored) {}

        // TiC / Modded weapon heuristics
        String className = item.getClass()
            .getName()
            .toLowerCase();
        String simpleName = item.getClass()
            .getSimpleName()
            .toLowerCase();

        if (className.contains("tconstruct") || className.contains("tinkers")) {
            return true;
        }

        if (simpleName.contains("sword") || simpleName.contains("blade")
            || simpleName.contains("axe")
            || simpleName.contains("dagger")
            || simpleName.contains("knife")
            || simpleName.contains("rapier")
            || simpleName.contains("scythe")
            || simpleName.contains("hammer")
            || simpleName.contains("spear")
            || simpleName.contains("halberd")
            || simpleName.contains("mace")
            || simpleName.contains("claymore")
            || simpleName.contains("katana")
            || simpleName.contains("saber")
            || simpleName.contains("pickaxe")
            || simpleName.contains("shovel")
            || simpleName.contains("spade")
            || simpleName.contains("hoe")
            || simpleName.contains("glaive")
            || simpleName.contains("warhammer")
            || simpleName.contains("sickle")) {
            return true;
        }

        return false;
    }

    private static ToolMaterial getToolMaterial(Item item) {
        if (item instanceof ItemSword) {
            try {
                return ToolMaterial.valueOf(((ItemSword) item).getToolMaterialName());
            } catch (Exception ignored) {}
        }
        if (item instanceof net.minecraft.item.ItemTool) {
            return ((net.minecraft.item.ItemTool) item).func_150913_i();
        }
        if (item instanceof ItemHoe) {
            try {
                return ToolMaterial.valueOf(((ItemHoe) item).getToolMaterialName());
            } catch (Exception ignored) {}
        }
        return ToolMaterial.IRON;
    }

    public static boolean isRebalanceEligible(Item item) {
        if (item == null) {
            return false;
        }
        if (item instanceof ItemSword || item instanceof ItemAxe
            || item instanceof ItemPickaxe
            || item instanceof ItemSpade) {
            return true;
        }
        String regName = getItemRegistryName(item);
        if (regName != null && CUSTOM_DAMAGES.containsKey(regName)) {
            return true;
        }
        return false;
    }

    public static double getRebalancedDamage(Item item, ItemStack stack) {
        if (item == null) {
            return -1.0;
        }

        String regName = getItemRegistryName(item);
        if (regName != null && CUSTOM_DAMAGES.containsKey(regName)) {
            return Math.max(0.0, (double) CUSTOM_DAMAGES.get(regName) - 1.0);
        }

        ToolMaterial mat = getToolMaterial(item);

        if (item instanceof ItemSword) {
            if (mat == ToolMaterial.WOOD || mat == ToolMaterial.GOLD) return 3.0; // Total 4.0
            if (mat == ToolMaterial.STONE) return 4.0; // Total 5.0
            if (mat == ToolMaterial.IRON) return 5.0; // Total 6.0
            if (mat == ToolMaterial.EMERALD) return 6.0; // Total 7.0
            if (mat != null && mat.name()
                .toLowerCase()
                .contains("netherite")) return 7.0; // Total 8.0
            return 3.0 + (mat != null ? (double) mat.getDamageVsEntity() : 2.0);
        }

        if (item instanceof ItemAxe) {
            if (mat == ToolMaterial.WOOD || mat == ToolMaterial.GOLD) return 6.0; // Total 7.0
            if (mat == ToolMaterial.STONE) return 8.0; // Total 9.0
            if (mat == ToolMaterial.IRON) return 8.0; // Total 9.0
            if (mat == ToolMaterial.EMERALD) return 8.0; // Total 9.0
            if (mat != null && mat.name()
                .toLowerCase()
                .contains("netherite")) return 9.0; // Total 10.0
            return Math.max(6.0, 5.0 + (mat != null ? (double) mat.getDamageVsEntity() : 2.0));
        }

        if (item instanceof ItemPickaxe) {
            if (mat == ToolMaterial.WOOD || mat == ToolMaterial.GOLD) return 1.0; // Total 2.0
            if (mat == ToolMaterial.STONE) return 2.0; // Total 3.0
            if (mat == ToolMaterial.IRON) return 3.0; // Total 4.0
            if (mat == ToolMaterial.EMERALD) return 4.0; // Total 5.0
            if (mat != null && mat.name()
                .toLowerCase()
                .contains("netherite")) return 5.0; // Total 6.0
            return 1.0 + (mat != null ? (double) mat.getDamageVsEntity() : 2.0);
        }

        if (item instanceof ItemSpade) {
            if (mat == ToolMaterial.WOOD || mat == ToolMaterial.GOLD) return 1.5; // Total 2.5
            if (mat == ToolMaterial.STONE) return 2.5; // Total 3.5
            if (mat == ToolMaterial.IRON) return 3.5; // Total 4.5
            if (mat == ToolMaterial.EMERALD) return 4.5; // Total 5.5
            if (mat != null && mat.name()
                .toLowerCase()
                .contains("netherite")) return 5.5; // Total 6.5
            return 1.5 + (mat != null ? (double) mat.getDamageVsEntity() : 2.0);
        }

        return -1.0;
    }
}
