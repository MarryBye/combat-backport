package com.marrybye.combatbackport.client;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import com.marrybye.combatbackport.Config;
import com.marrybye.combatbackport.combat.WeaponRegistry;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TooltipHandler {

    public static final TooltipHandler INSTANCE = new TooltipHandler();
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(
        "#.##",
        DecimalFormatSymbols.getInstance(Locale.ROOT));

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onItemTooltip(ItemTooltipEvent event) {
        if (!Config.enableAttackCooldown) {
            return;
        }

        ItemStack stack = event.itemStack;
        if (stack == null || stack.getItem() == null) {
            return;
        }

        if (!WeaponRegistry.isWeaponOrTool(stack)) {
            return;
        }

        float speed = WeaponRegistry.getAttackSpeed(stack);
        String formattedSpeed = DECIMAL_FORMAT.format(speed);
        String speedLabel = StatCollector.translateToLocal("attribute.name.generic.attackSpeed");
        if (speedLabel == null || speedLabel.equals("attribute.name.generic.attackSpeed")) {
            speedLabel = "Attack Speed";
        }

        // Vanilla 1.9+ formatting style: " 1.6 Attack Speed" (in dark green / blue)
        String speedLine = EnumChatFormatting.DARK_GREEN + " " + formattedSpeed + " " + speedLabel;

        List<String> tooltip = event.toolTip;
        if (tooltip == null) {
            return;
        }

        // Check if tooltip already contains attack speed to avoid duplication
        for (String line : tooltip) {
            if (line.contains(speedLabel)) {
                return;
            }
        }

        // Search for attack damage line in tooltip to insert attack speed right below it
        int insertIndex = -1;
        String localizedAttackDamage = StatCollector.translateToLocal("attribute.name.generic.attackDamage");

        for (int i = 0; i < tooltip.size(); i++) {
            String line = tooltip.get(i);
            if ((localizedAttackDamage != null && !localizedAttackDamage.isEmpty()
                && line.contains(localizedAttackDamage)) || line.contains("Attack Damage")
                || line.contains("Урон")
                || line.toLowerCase(Locale.ROOT)
                    .contains("damage")) {
                insertIndex = i + 1;
                break;
            }
        }

        if (insertIndex >= 0 && insertIndex <= tooltip.size()) {
            tooltip.add(insertIndex, speedLine);
        } else {
            // If no damage line found (e.g. TiC tool or modded weapon with custom lore), append it cleanly
            tooltip.add(speedLine);
        }
    }
}
