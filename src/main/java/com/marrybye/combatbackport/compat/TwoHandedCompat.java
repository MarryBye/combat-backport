package com.marrybye.combatbackport.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemBucketMilk;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.marrybye.combatbackport.Config;
import com.marrybye.combatbackport.api.ICombatPlayer;
import com.marrybye.combatbackport.combat.WeaponRegistry;
import com.marrybye.combatbackport.combat.item.ItemShield;

import cpw.mods.fml.common.Loader;

public class TwoHandedCompat {

    private static final Map<Item, Boolean> CACHED_TWO_HANDED = new HashMap<>();
    private static final Map<Item, Boolean> CACHED_RANGED = new HashMap<>();
    private static final Map<Class<?>, Method> CACHED_METHODS = new HashMap<>();
    private static final Method NO_METHOD;

    static {
        Method dummy = null;
        try {
            dummy = Object.class.getMethod("hashCode");
        } catch (Exception ignored) {}
        NO_METHOD = dummy;
    }

    public static final Set<String> CUSTOM_TWO_HANDED = new HashSet<>();
    public static final Set<String> CUSTOM_RANGED = new HashSet<>();

    private static Field alwaysEdibleField = null;
    private static boolean alwaysEdibleFieldChecked = false;

    public static void clearCache() {
        CACHED_TWO_HANDED.clear();
        CACHED_RANGED.clear();
        CACHED_METHODS.clear();
    }

    public static void init(String[] twoHanded, String[] ranged) {
        clearCache();
        CUSTOM_TWO_HANDED.clear();
        if (twoHanded != null) {
            for (String s : twoHanded) {
                if (s != null && !s.trim()
                    .isEmpty()) {
                    CUSTOM_TWO_HANDED.add(
                        s.trim()
                            .toLowerCase());
                }
            }
        }
        CUSTOM_RANGED.clear();
        if (ranged != null) {
            for (String s : ranged) {
                if (s != null && !s.trim()
                    .isEmpty()) {
                    CUSTOM_RANGED.add(
                        s.trim()
                            .toLowerCase());
                }
            }
        }
    }

    /**
     * Determines whether the given item stack is a two-handed weapon.
     */
    public static boolean isTwoHanded(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }
        Item item = stack.getItem();
        if (item instanceof ItemShield) {
            return false;
        }

        // 1. Configured custom list
        String regName = WeaponRegistry.getItemRegistryName(item);
        if (regName != null && CUSTOM_TWO_HANDED.contains(regName.toLowerCase())) {
            return true;
        }

        // 2. NBT Tags
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag.hasKey("TwoHanded") && (tag.getBoolean("TwoHanded") || tag.getInteger("TwoHanded") > 0))
                return true;
            if (tag.hasKey("two_handed") && tag.getBoolean("two_handed")) return true;
            if (tag.hasKey("isTwoHanded") && tag.getBoolean("isTwoHanded")) return true;
            if (tag.hasKey("2handed") && tag.getBoolean("2handed")) return true;
            if (tag.hasKey("TwoHand") && tag.getBoolean("TwoHand")) return true;
            if (tag.hasKey("weaponType") && tag.getString("weaponType")
                .toLowerCase()
                .contains("two_hand")) return true;
            if (tag.hasKey("HandType") && tag.getString("HandType")
                .equalsIgnoreCase("two_handed")) return true;
        }

        // 3. Cache check
        Boolean cached = CACHED_TWO_HANDED.get(item);
        if (cached != null) {
            return cached;
        }

        boolean result = calculateIsTwoHanded(item, stack, regName);
        CACHED_TWO_HANDED.put(item, result);
        return result;
    }

    private static boolean calculateIsTwoHanded(Item item, ItemStack stack, String regName) {
        String className = item.getClass()
            .getName();
        String simpleName = item.getClass()
            .getSimpleName();

        // Tinkers' Construct known 2-handed weapons
        if (className.contains("tconstruct") || className.contains("tinkers")) {
            if (simpleName.equalsIgnoreCase("Cleaver") || simpleName.equalsIgnoreCase("Battleaxe")
                || simpleName.equalsIgnoreCase("LumberAxe")
                || simpleName.equalsIgnoreCase("Hammer")
                || simpleName.equalsIgnoreCase("Excavator")
                || simpleName.equalsIgnoreCase("Scythe")) {
                return true;
            }
        }

        // Check interfaces (e.g. ITwoHanded, ITwoHandedWeapon, IBattlegearWeapon)
        for (Class<?> iface : item.getClass()
            .getInterfaces()) {
            String name = iface.getSimpleName();
            if (name.equalsIgnoreCase("ITwoHanded") || name.equalsIgnoreCase("ITwoHandedWeapon")) {
                return true;
            }
        }

        // Check reflected isTwoHanded method
        Method m = getCachedMethod(item.getClass());
        if (m != null && m != NO_METHOD) {
            try {
                Object res;
                if (m.getParameterTypes().length == 1) {
                    res = m.invoke(item, stack);
                } else {
                    res = m.invoke(item);
                }
                if (Boolean.TRUE.equals(res)) {
                    return true;
                }
            } catch (Throwable ignored) {}
        }

        // Name heuristics (class name, unlocalized name, registry name)
        String unloc = "";
        try {
            unloc = item.getUnlocalizedName(stack);
        } catch (Throwable ignored) {}

        String combined = (simpleName + " " + (unloc != null ? unloc : "") + " " + (regName != null ? regName : ""))
            .toLowerCase();

        if (combined.contains("greatsword") || combined.contains("great_sword")
            || combined.contains("cleaver")
            || combined.contains("battleaxe")
            || combined.contains("battle_axe")
            || combined.contains("warhammer")
            || combined.contains("war_hammer")
            || combined.contains("halberd")
            || combined.contains("polearm")
            || (combined.contains("spear") && !combined.contains("spearmint"))
            || combined.contains("lance")
            || combined.contains("pike")
            || combined.contains("claymore")
            || combined.contains("scythe")
            || combined.contains("zweihander")
            || combined.contains("quarterstaff")
            || combined.contains("twohanded")
            || combined.contains("two_handed")
            || combined.contains("2handed")
            || combined.contains("heavyweapon")
            || combined.contains("heavy_weapon")
            || combined.contains("bigsword")
            || combined.contains("giant_sword")) {
            return true;
        }

        return false;
    }

    private static Method getCachedMethod(Class<?> clazz) {
        Method m = CACHED_METHODS.get(clazz);
        if (m != null) {
            return m;
        }
        Method found = NO_METHOD;
        for (Method method : clazz.getMethods()) {
            if (method.getName()
                .equalsIgnoreCase("isTwoHanded")
                || method.getName()
                    .equalsIgnoreCase("twoHanded")) {
                if (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class) {
                    Class<?>[] params = method.getParameterTypes();
                    if (params.length == 0 || (params.length == 1 && params[0].isAssignableFrom(ItemStack.class))) {
                        found = method;
                        found.setAccessible(true);
                        break;
                    }
                }
            }
        }
        CACHED_METHODS.put(clazz, found);
        return found;
    }

    /**
     * Determines whether the given item stack is a ranged weapon (bow, crossbow, gun, musket, etc.).
     */
    public static boolean isRangedWeapon(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }
        Item item = stack.getItem();
        if (item instanceof ItemShield) {
            return false;
        }

        // 1. Configured custom list
        String regName = WeaponRegistry.getItemRegistryName(item);
        if (regName != null && CUSTOM_RANGED.contains(regName.toLowerCase())) {
            return true;
        }

        // 2. Direct vanilla / standard checks
        if (item instanceof ItemBow) {
            return true;
        }
        try {
            if (item.getItemUseAction(stack) == EnumAction.bow) {
                return true;
            }
        } catch (Throwable ignored) {}

        // 3. Cache check
        Boolean cached = CACHED_RANGED.get(item);
        if (cached != null) {
            return cached;
        }

        boolean result = calculateIsRanged(item, stack, regName);
        CACHED_RANGED.put(item, result);
        return result;
    }

    private static boolean calculateIsRanged(Item item, ItemStack stack, String regName) {
        String className = item.getClass()
            .getName();
        String simpleName = item.getClass()
            .getSimpleName();

        // Tinkers' Construct ranged weapons
        if (className.contains("tconstruct") || className.contains("tinkers")) {
            if (simpleName.equalsIgnoreCase("Shortbow") || simpleName.equalsIgnoreCase("Longbow")
                || simpleName.equalsIgnoreCase("Crossbow")
                || simpleName.equalsIgnoreCase("AmmoWeapon")
                || simpleName.equalsIgnoreCase("BowCore")) {
                return true;
            }
        }

        String unloc = "";
        try {
            unloc = item.getUnlocalizedName(stack);
        } catch (Throwable ignored) {}

        String combined = (simpleName + " " + (unloc != null ? unloc : "") + " " + (regName != null ? regName : ""))
            .toLowerCase();

        // Bow (excluding bowl, elbow, rainbow)
        if (combined.contains("bow") && !combined.contains("bowl")
            && !combined.contains("elbow")
            && !combined.contains("rainbow")) {
            return true;
        }

        if (combined.contains("crossbow") || combined.contains("musket")
            || combined.contains("blunderbuss")
            || combined.contains("rifle")
            || combined.contains("shotgun")
            || combined.contains("pistol")
            || combined.contains("revolver")
            || (combined.contains("gun") && !combined.contains("gunpowder"))
            || combined.contains("slingshot")
            || combined.contains("blowgun")
            || combined.contains("blaster")
            || combined.contains("cannon")
            || combined.contains("bazooka")
            || combined.contains("firearm")) {
            return true;
        }

        return false;
    }

    /**
     * Determines whether the given item is food, drink, or a consumable item.
     */
    public static boolean isFoodOrDrink(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }
        Item item = stack.getItem();
        if (item instanceof ItemFood || item instanceof ItemBucketMilk) {
            return true;
        }
        if (item instanceof ItemPotion) {
            return !ItemPotion.isSplash(stack.getItemDamage());
        }
        try {
            EnumAction action = item.getItemUseAction(stack);
            if (action == EnumAction.eat || action == EnumAction.drink) {
                return true;
            }
        } catch (Throwable ignored) {}

        String name = item.getClass()
            .getSimpleName()
            .toLowerCase();
        return name.contains("food") || name.contains("edible") || name.contains("drink") || name.contains("beverage");
    }

    /**
     * Checks if the player can currently consume the given food or drink item.
     */
    public static boolean canPlayerConsume(EntityPlayer player, ItemStack stack) {
        if (player == null || stack == null || stack.getItem() == null) {
            return false;
        }
        Item item = stack.getItem();

        if (item instanceof ItemFood) {
            ItemFood food = (ItemFood) item;
            if (isAlwaysEdible(food, stack)) {
                return true;
            }
            return player.canEat(false);
        }

        if (item instanceof ItemBucketMilk) {
            return true;
        }

        if (item instanceof ItemPotion) {
            return !ItemPotion.isSplash(stack.getItemDamage());
        }

        try {
            EnumAction action = item.getItemUseAction(stack);
            if (action == EnumAction.eat) {
                return player.canEat(false);
            }
            if (action == EnumAction.drink) {
                return true;
            }
        } catch (Throwable ignored) {}

        return false;
    }

    private static boolean isAlwaysEdible(ItemFood food, ItemStack stack) {
        if (food == Items.golden_apple) {
            return true;
        }
        if (!alwaysEdibleFieldChecked) {
            alwaysEdibleFieldChecked = true;
            for (String fieldName : new String[] { "alwaysEdible", "field_77852_b", "b" }) {
                try {
                    Field f = ItemFood.class.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    alwaysEdibleField = f;
                    break;
                } catch (Throwable ignored) {}
            }
        }
        if (alwaysEdibleField != null) {
            try {
                return alwaysEdibleField.getBoolean(food);
            } catch (Throwable ignored) {}
        }
        return false;
    }

    /**
     * Gets the player's main-hand item (even if Backhand temporarily swapped slots).
     */
    public static ItemStack getMainHandItem(EntityPlayer player) {
        if (player == null) {
            return null;
        }
        if (Loader.isModLoaded("backhand")) {
            try {
                if (BackhandHelper.isUsingOffhand(player)) {
                    ItemStack main = BackhandHelper.getMainhand(player);
                    if (main != null) {
                        return main;
                    }
                }
            } catch (Throwable ignored) {}
        }
        return player.getHeldItem();
    }

    /**
     * Gets the player's offhand item (Backhand mod support).
     */
    public static ItemStack getOffHandItem(EntityPlayer player) {
        if (player == null) {
            return null;
        }
        if (Loader.isModLoaded("backhand")) {
            try {
                return BackhandHelper.getOffhand(player);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    /**
     * Determines whether the given item stack is in the player's offhand.
     */
    public static boolean isOffhand(EntityPlayer player, ItemStack stack) {
        if (player == null || stack == null) {
            return false;
        }
        if (Loader.isModLoaded("backhand")) {
            try {
                if (BackhandHelper.isUsingOffhand(player)) {
                    return true;
                }
                ItemStack offhand = BackhandHelper.getOffhand(player);
                if (offhand == stack) {
                    return true;
                }
                if (offhand != null && ItemStack.areItemStacksEqual(offhand, stack)) {
                    ItemStack main = getMainHandItem(player);
                    if (main != stack) {
                        return true;
                    }
                }
            } catch (Throwable ignored) {}
        }
        return false;
    }

    /**
     * Checks if the offhand shield should be tucked away (lowered down out of view).
     */
    public static boolean shouldTuckAwayShield(EntityPlayer player, ItemStack mainHand) {
        if (!Config.enableShieldTuckAway) {
            return false;
        }
        if (mainHand == null || mainHand.getItem() == null) {
            return false;
        }

        // Two-handed weapon held
        if (isTwoHanded(mainHand)) {
            return true;
        }

        // Ranged weapon held
        if (isRangedWeapon(mainHand)) {
            return true;
        }

        // Player is actively using a two-handed or ranged item
        if (player != null && player.isUsingItem()) {
            ItemStack inUse = player.getItemInUse();
            if (inUse != null && (isTwoHanded(inUse) || isRangedWeapon(inUse))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether the shield can be used to block by the player.
     */
    public static boolean canUseShield(EntityPlayer player, ItemStack shieldStack) {
        if (player == null) {
            return false;
        }

        // Disabled cooldown check
        if (player instanceof ICombatPlayer) {
            if (((ICombatPlayer) player).getShieldCooldown() > 0) {
                return false;
            }
        }

        // If shield is held in offhand or another item is in the main hand:
        ItemStack mainHand = getMainHandItem(player);
        if (mainHand != null && mainHand != shieldStack) {
            // Holding a two-handed weapon: cannot block
            if (isTwoHanded(mainHand)) {
                return false;
            }

            // Holding a ranged weapon: cannot block
            if (isRangedWeapon(mainHand)) {
                return false;
            }

            // Actively using mainhand item: cannot block
            if (player.isUsingItem() && player.getItemInUse() != shieldStack) {
                return false;
            }

            // Mainhand has food/drink that player can consume: food takes priority
            if (isFoodOrDrink(mainHand) && canPlayerConsume(player, mainHand)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Isolated helper to prevent ClassNotFoundException when Backhand is not loaded.
     */
    private static class BackhandHelper {

        static ItemStack getOffhand(EntityPlayer player) {
            return xonin.backhand.api.core.BackhandUtils.getOffhandItem(player);
        }

        static boolean isUsingOffhand(EntityPlayer player) {
            return xonin.backhand.api.core.BackhandUtils.isUsingOffhand(player);
        }

        static ItemStack getMainhand(EntityPlayer player) {
            if (player instanceof xonin.backhand.api.core.IBackhandPlayer) {
                return ((xonin.backhand.api.core.IBackhandPlayer) player).getMainhandItem();
            }
            return null;
        }
    }
}
