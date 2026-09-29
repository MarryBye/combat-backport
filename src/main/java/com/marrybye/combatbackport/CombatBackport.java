package com.marrybye.combatbackport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(
    modid = CombatBackport.MODID,
    version = Tags.VERSION,
    name = "Combat Backport",
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "after:angelica;after:backhand")
public class CombatBackport {

    public static final String MODID = "combatbackport";
    public static final Logger LOG = LogManager.getLogger(MODID);

    public static com.marrybye.combatbackport.combat.enchantment.EnchantmentSweepingEdge sweepingEdgeEnchantment;
    public static com.marrybye.combatbackport.combat.enchantment.EnchantmentMending mendingEnchantment;
    public static com.marrybye.combatbackport.combat.item.ItemShield shield;

    @SidedProxy(
        clientSide = "com.marrybye.combatbackport.ClientProxy",
        serverSide = "com.marrybye.combatbackport.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }
}
