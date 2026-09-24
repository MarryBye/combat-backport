package com.marrybye.combatbackport.mixins;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionsRowList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mixin(GuiOptionsRowList.Row.class)
public interface MixinGuiOptionsRowListRow {

    @Accessor("field_148323_b")
    GuiButton combatbackport$getButtonA();

    @Accessor("field_148324_c")
    GuiButton combatbackport$getButtonB();
}
