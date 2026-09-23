package com.marrybye.combatbackport.client;

import java.util.Collections;

import com.marrybye.combatbackport.Config;
import com.marrybye.combatbackport.combat.AttackIndicatorMode;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AngelicaIntegration {

    public static void init() {
        if (Loader.isModLoaded("angelica")) {
            try {
                AngelicaHelper.register();
            } catch (Throwable ignored) {}
        }
    }

    private static class AngelicaHelper {

        public static void register() {
            org.taumc.celeritas.api.OptionGUIConstructionEvent.BUS.addListener(event -> {
                org.taumc.celeritas.api.options.structure.OptionStorage<Object> storage = new org.taumc.celeritas.api.options.structure.OptionStorage<Object>() {

                    @Override
                    public Object getData() {
                        return this;
                    }

                    @Override
                    public void save() {
                        Config.saveConfig();
                    }
                };

                org.taumc.celeritas.api.options.structure.Option<AttackIndicatorMode> indicatorOption = org.taumc.celeritas.api.options.structure.OptionImpl
                    .createBuilder(AttackIndicatorMode.class, storage)
                    .setId(
                        org.taumc.celeritas.api.options.OptionIdentifier
                            .create("combatbackport", "attack_indicator", AttackIndicatorMode.class))
                    .setName(org.embeddedt.embeddium.impl.gui.framework.TextComponent.literal("Attack Indicator"))
                    .setTooltip(
                        org.embeddedt.embeddium.impl.gui.framework.TextComponent
                            .literal("Position of the weapon attack cooldown recharge meter."))
                    .setControl(
                        opt -> new org.taumc.celeritas.api.options.control.CyclingControl<>(
                            opt,
                            AttackIndicatorMode.class,
                            new org.embeddedt.embeddium.impl.gui.framework.TextComponent[] {
                                org.embeddedt.embeddium.impl.gui.framework.TextComponent.literal("Disabled"),
                                org.embeddedt.embeddium.impl.gui.framework.TextComponent.literal("Crosshair"),
                                org.embeddedt.embeddium.impl.gui.framework.TextComponent.literal("Hotbar") }))
                    .setBinding((s, value) -> {
                        Config.attackIndicatorMode = value;
                        Config.saveConfig();
                    }, s -> Config.attackIndicatorMode)
                    .build();

                org.taumc.celeritas.api.options.structure.OptionGroup combatGroup = new org.taumc.celeritas.api.options.structure.OptionGroup.Builder()
                    .setId(org.taumc.celeritas.api.options.OptionIdentifier.create("combatbackport", "combat_group"))
                    .add(indicatorOption)
                    .build();

                org.taumc.celeritas.api.options.structure.OptionPage combatPage = new org.taumc.celeritas.api.options.structure.OptionPage(
                    org.taumc.celeritas.api.options.OptionIdentifier.create("combatbackport", "combat_page"),
                    org.embeddedt.embeddium.impl.gui.framework.TextComponent.literal("Combat"),
                    Collections.singletonList(combatGroup));

                event.addPage(combatPage);
            });
        }
    }
}
