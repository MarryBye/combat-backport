package com.marrybye.combatbackport.mixins;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableList;
import com.marrybye.combatbackport.Config;
import com.marrybye.combatbackport.combat.AttackIndicatorMode;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages;
import me.jellysquid.mods.sodium.client.gui.options.Option;
import me.jellysquid.mods.sodium.client.gui.options.OptionGroup;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import me.jellysquid.mods.sodium.client.gui.options.control.CyclingControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage;

@Pseudo
@SideOnly(Side.CLIENT)
@Mixin(value = SodiumGameOptionPages.class, remap = false)
public abstract class MixinSodiumGameOptionPages {

    @Inject(method = "general", at = @At("RETURN"), cancellable = true, remap = false)
    private static void combatbackport$addAttackIndicatorOption(CallbackInfoReturnable<OptionPage> cir) {
        OptionPage original = cir.getReturnValue();
        if (original == null) return;

        List<OptionGroup> groups = new ArrayList<>(original.getGroups());
        groups.add(combatbackport$createCombatGroup());
        cir.setReturnValue(new OptionPage(original.getName(), ImmutableList.copyOf(groups)));
    }

    @Unique
    private static OptionGroup combatbackport$createCombatGroup() {
        OptionStorage<Object> storage = new OptionStorage<Object>() {

            @Override
            public Object getData() {
                return this;
            }

            @Override
            public void save() {
                Config.saveConfig();
            }
        };

        Option<AttackIndicatorMode> indicatorOption = OptionImpl.createBuilder(AttackIndicatorMode.class, storage)
            .setName(I18n.format("options.combatbackport.attackIndicator"))
            .setTooltip(I18n.format("options.combatbackport.attackIndicator.tooltip"))
            .setControl(
                opt -> new CyclingControl<>(
                    opt,
                    AttackIndicatorMode.class,
                    new AttackIndicatorMode[] { AttackIndicatorMode.DISABLED, AttackIndicatorMode.CROSSHAIR,
                        AttackIndicatorMode.HOTBAR },
                    new String[] { I18n.format("options.off"), I18n.format("options.combatbackport.crosshair"),
                        I18n.format("options.combatbackport.hotbar") }))
            .setBinding((s, value) -> {
                Config.attackIndicatorMode = value;
                Config.saveConfig();
            }, s -> Config.attackIndicatorMode)
            .build();

        return OptionGroup.createBuilder()
            .add(indicatorOption)
            .build();
    }
}
