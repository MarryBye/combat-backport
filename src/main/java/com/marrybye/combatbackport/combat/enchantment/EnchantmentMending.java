package com.marrybye.combatbackport.combat.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;

public class EnchantmentMending extends Enchantment {

    public EnchantmentMending(int id, int weight) {
        super(id, weight, EnumEnchantmentType.breakable);
        this.setName("combatbackport.mending");
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinEnchantability(int level) {
        return 25;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return 75;
    }

    @Override
    public boolean canApply(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return stack.isItemStackDamageable() || super.canApply(stack);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false; // Treasure enchantment!
    }

    @Override
    public boolean isAllowedOnBooks() {
        return true;
    }
}
