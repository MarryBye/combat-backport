package com.marrybye.combatbackport.combat.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

import com.marrybye.combatbackport.combat.WeaponRegistry;

public class EnchantmentSweepingEdge extends Enchantment {

    public EnchantmentSweepingEdge(int id, int weight) {
        super(id, weight, EnumEnchantmentType.weapon);
        this.setName("combatbackport.sweeping");
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinEnchantability(int level) {
        return 5 + (level - 1) * 9;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return this.getMinEnchantability(level) + 15;
    }

    @Override
    public boolean canApply(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }
        return stack.getItem() instanceof ItemSword || WeaponRegistry.canSweep(stack);
    }
}
