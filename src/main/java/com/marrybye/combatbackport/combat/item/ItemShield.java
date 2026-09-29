package com.marrybye.combatbackport.combat.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import com.marrybye.combatbackport.api.ICombatPlayer;

public class ItemShield extends Item {

    public ItemShield() {
        this.maxStackSize = 1;
        this.setMaxDamage(336);
        this.setCreativeTab(CreativeTabs.tabCombat);
        this.setUnlocalizedName("combatbackport.shield");
        this.setTextureName("combatbackport:shield");
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.block;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (player instanceof ICombatPlayer) {
            if (((ICombatPlayer) player).getShieldCooldown() > 0) {
                return stack;
            }
        }
        player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        return stack;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        if (repair == null) {
            return false;
        }
        int[] ids = OreDictionary.getOreIDs(repair);
        for (int id : ids) {
            if ("plankWood".equals(OreDictionary.getOreName(id))) {
                return true;
            }
        }
        return repair.getItem() == Item.getItemFromBlock(Blocks.planks);
    }

    @Override
    public int getItemEnchantability() {
        return 14;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        if (stack != null && stack.hasTagCompound()
            && stack.getTagCompound()
                .hasKey("BlockEntityTag")) {
            list.add(StatCollector.translateToLocal("tooltip.combatbackport.shield_pattern"));
        }
    }
}
