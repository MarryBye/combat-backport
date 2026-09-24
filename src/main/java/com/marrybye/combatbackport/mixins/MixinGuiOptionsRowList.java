package com.marrybye.combatbackport.mixins;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiOptionsRowList;
import net.minecraft.client.settings.GameSettings;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.marrybye.combatbackport.client.gui.GuiButtonAttackIndicator;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mixin(GuiOptionsRowList.class)
public abstract class MixinGuiOptionsRowList extends GuiListExtended {

    @Shadow
    @Final
    private List field_148184_k;

    public MixinGuiOptionsRowList(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void combatbackport$addAttackIndicatorOption(Minecraft mc, int width, int height, int top, int bottom,
        int slotHeight, GameSettings.Options[] options, CallbackInfo ci) {
        if (options == null || options.length == 0) return;

        boolean isVideoSettings = false;
        for (GameSettings.Options opt : options) {
            if (opt == GameSettings.Options.GRAPHICS || opt == GameSettings.Options.RENDER_DISTANCE) {
                isVideoSettings = true;
                break;
            }
        }

        if (isVideoSettings) {
            if (!this.field_148184_k.isEmpty()) {
                GuiOptionsRowList.Row lastRow = (GuiOptionsRowList.Row) this.field_148184_k
                    .get(this.field_148184_k.size() - 1);
                MixinGuiOptionsRowListRow rowAccessor = (MixinGuiOptionsRowListRow) (Object) lastRow;
                if (rowAccessor.combatbackport$getButtonB() == null) {
                    GuiButton btn = new GuiButtonAttackIndicator(9900, width / 2 - 155 + 160, 0);
                    this.field_148184_k.set(
                        this.field_148184_k.size() - 1,
                        new GuiOptionsRowList.Row(rowAccessor.combatbackport$getButtonA(), btn));
                } else {
                    GuiButton btn = new GuiButtonAttackIndicator(9900, width / 2 - 155, 0);
                    this.field_148184_k.add(new GuiOptionsRowList.Row(btn, null));
                }
            }
        }
    }
}
