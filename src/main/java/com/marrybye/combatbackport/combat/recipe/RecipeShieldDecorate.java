package com.marrybye.combatbackport.combat.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.marrybye.combatbackport.CombatBackport;
import com.marrybye.combatbackport.combat.item.ItemShield;

public class RecipeShieldDecorate implements IRecipe {

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        ItemStack shield = null;
        ItemStack banner = null;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack == null) {
                continue;
            }

            if (stack.getItem() instanceof ItemShield) {
                if (shield != null) {
                    return false;
                }
                // Shield can only be decorated if it doesn't already have a pattern
                if (stack.hasTagCompound() && stack.getTagCompound()
                    .hasKey("BlockEntityTag")) {
                    return false;
                }
                shield = stack;
            } else if (isBanner(stack)) {
                if (banner != null) {
                    return false;
                }
                banner = stack;
            } else {
                return false;
            }
        }

        return shield != null && banner != null;
    }

    private boolean isBanner(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }
        String unloc = stack.getItem()
            .getUnlocalizedName();
        if (unloc != null && unloc.toLowerCase()
            .contains("banner")) {
            return true;
        }
        String className = stack.getItem()
            .getClass()
            .getSimpleName();
        return className.toLowerCase()
            .contains("banner");
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack shield = null;
        ItemStack banner = null;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack == null) {
                continue;
            }
            if (stack.getItem() instanceof ItemShield) {
                shield = stack;
            } else if (isBanner(stack)) {
                banner = stack;
            }
        }

        if (shield == null || banner == null) {
            return null;
        }

        ItemStack result = shield.copy();
        result.stackSize = 1;

        NBTTagCompound tag = result.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            result.setTagCompound(tag);
        }

        if (banner.hasTagCompound() && banner.getTagCompound()
            .hasKey("BlockEntityTag")) {
            tag.setTag(
                "BlockEntityTag",
                banner.getTagCompound()
                    .getCompoundTag("BlockEntityTag")
                    .copy());
        } else {
            NBTTagCompound blockEntityTag = new NBTTagCompound();
            blockEntityTag.setInteger("Base", banner.getItemDamage() & 15);
            tag.setTag("BlockEntityTag", blockEntityTag);
        }

        return result;
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        if (CombatBackport.shield != null) {
            return new ItemStack(CombatBackport.shield);
        }
        return null;
    }
}
