package com.marrybye.combatbackport.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import com.marrybye.combatbackport.Config;
import com.marrybye.combatbackport.combat.AttackIndicatorMode;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonAttackIndicator extends GuiButton {

    public GuiButtonAttackIndicator(int id, int x, int y) {
        super(id, x, y, 150, 20, getButtonText());
    }

    public static String getButtonText() {
        String name = I18n.format("options.combatbackport.attackIndicator");
        if (name == null || name.equals("options.combatbackport.attackIndicator")) {
            name = "Attack Indicator";
        }
        String value;
        switch (Config.attackIndicatorMode) {
            case CROSSHAIR:
                value = I18n.format("options.combatbackport.crosshair");
                if (value == null || value.equals("options.combatbackport.crosshair")) {
                    value = "Crosshair";
                }
                break;
            case HOTBAR:
                value = I18n.format("options.combatbackport.hotbar");
                if (value == null || value.equals("options.combatbackport.hotbar")) {
                    value = "Hotbar";
                }
                break;
            case DISABLED:
            default:
                value = I18n.format("options.off");
                if (value == null || value.equals("options.off")) {
                    value = "OFF";
                }
                break;
        }
        return name + ": " + value;
    }

    public void cycle() {
        switch (Config.attackIndicatorMode) {
            case CROSSHAIR:
                Config.attackIndicatorMode = AttackIndicatorMode.HOTBAR;
                break;
            case HOTBAR:
                Config.attackIndicatorMode = AttackIndicatorMode.DISABLED;
                break;
            case DISABLED:
            default:
                Config.attackIndicatorMode = AttackIndicatorMode.CROSSHAIR;
                break;
        }
        Config.saveConfig();
        this.displayString = getButtonText();
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean clicked = super.mousePressed(mc, mouseX, mouseY);
        if (clicked) {
            cycle();
            mc.getSoundHandler()
                .playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
        }
        return clicked;
    }
}
