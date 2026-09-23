package com.marrybye.combatbackport;

import net.minecraftforge.common.MinecraftForge;

import com.marrybye.combatbackport.client.AngelicaIntegration;
import com.marrybye.combatbackport.client.AttackIndicatorRenderer;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(AttackIndicatorRenderer.INSTANCE);
        MinecraftForge.EVENT_BUS.register(com.marrybye.combatbackport.client.TooltipHandler.INSTANCE);
        AngelicaIntegration.init();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }
}
