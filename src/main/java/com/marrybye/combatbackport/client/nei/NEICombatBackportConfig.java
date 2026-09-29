package com.marrybye.combatbackport.client.nei;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.marrybye.combatbackport.CombatBackport;
import com.marrybye.combatbackport.Config;
import com.marrybye.combatbackport.Tags;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.registry.GameRegistry;

@Optional.Interface(iface = "codechicken.nei.api.IConfigureNEI", modid = "NotEnoughItems")
public class NEICombatBackportConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        if (!Config.enableShield || CombatBackport.shield == null) {
            try {
                if (CombatBackport.shield != null) {
                    API.hideItem(new ItemStack(CombatBackport.shield));
                }
            } catch (Throwable ignored) {}
            return;
        }

        Item banner = GameRegistry.findItem("etfuturum", "banner");
        if (banner == null) {
            banner = GameRegistry.findItem("ganyssurface", "banner");
        }
        if (banner != null) {
            try {
                ShieldDecorationRecipeHandler handler = new ShieldDecorationRecipeHandler();
                API.registerRecipeHandler(handler);
                API.registerUsageHandler(handler);
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public String getName() {
        return "Combat Backport NEI Integration";
    }

    @Override
    public String getVersion() {
        return Tags.VERSION;
    }
}
