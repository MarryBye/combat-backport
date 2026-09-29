package com.marrybye.combatbackport.client.nei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.marrybye.combatbackport.CombatBackport;

import codechicken.nei.recipe.ShapelessRecipeHandler;
import cpw.mods.fml.common.registry.GameRegistry;

public class ShieldDecorationRecipeHandler extends ShapelessRecipeHandler {

    private static Item getBannerItem() {
        Item item = GameRegistry.findItem("etfuturum", "banner");
        if (item == null) {
            item = GameRegistry.findItem("ganyssurface", "banner");
        }
        return item;
    }

    private static ItemStack getSampleBanner() {
        Item banner = getBannerItem();
        if (banner != null) {
            return new ItemStack(banner, 1, 1); // Red banner
        }
        return null;
    }

    private static ItemStack getSampleDecoratedShield() {
        if (CombatBackport.shield == null) {
            return null;
        }
        ItemStack shield = new ItemStack(CombatBackport.shield);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound blockEntityTag = new NBTTagCompound();
        blockEntityTag.setInteger("Base", 1);
        tag.setTag("BlockEntityTag", blockEntityTag);
        shield.setTagCompound(tag);
        return shield;
    }

    @Override
    public String getRecipeName() {
        return "Shield Decoration";
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        if (CombatBackport.shield == null || result == null) {
            return;
        }
        if (result.getItem() == CombatBackport.shield && result.hasTagCompound()
            && result.getTagCompound()
                .hasKey("BlockEntityTag")) {
            ItemStack banner = getSampleBanner();
            if (banner != null) {
                List<ItemStack> inputs = new ArrayList<>();
                inputs.add(new ItemStack(CombatBackport.shield));
                inputs.add(banner);
                arecipes.add(new CachedShapelessRecipe(inputs, result));
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (CombatBackport.shield == null || ingredient == null) {
            return;
        }
        Item banner = getBannerItem();
        if (ingredient.getItem() == CombatBackport.shield && !ingredient.hasTagCompound()) {
            ItemStack bannerStack = getSampleBanner();
            ItemStack output = getSampleDecoratedShield();
            if (bannerStack != null && output != null) {
                List<ItemStack> inputs = new ArrayList<>();
                inputs.add(ingredient);
                inputs.add(bannerStack);
                arecipes.add(new CachedShapelessRecipe(inputs, output));
            }
        } else if (banner != null && ingredient.getItem() == banner) {
            ItemStack output = getSampleDecoratedShield();
            if (output != null) {
                List<ItemStack> inputs = new ArrayList<>();
                inputs.add(new ItemStack(CombatBackport.shield));
                inputs.add(ingredient);
                arecipes.add(new CachedShapelessRecipe(inputs, output));
            }
        }
    }
}
