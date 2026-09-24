package com.marrybye.combatbackport.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.marrybye.combatbackport.Config;
import com.marrybye.combatbackport.api.ICombatPlayer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Shadow
    private Minecraft mc;

    @Shadow
    private ItemStack itemToRender;

    @Shadow
    private float equippedProgress;

    @Shadow
    private float prevEquippedProgress;

    @Shadow
    private int equippedItemSlot;

    @Inject(method = "updateEquippedItem", at = @At("HEAD"), cancellable = true)
    private void combatbackport$onUpdateEquippedItem(CallbackInfo ci) {
        this.prevEquippedProgress = this.equippedProgress;
        if (this.mc == null || this.mc.thePlayer == null) {
            return;
        }

        EntityClientPlayerMP player = this.mc.thePlayer;
        ItemStack currentItem = player.inventory.getCurrentItem();
        int currentSlot = player.inventory.currentItem;

        boolean sameItem = (this.equippedItemSlot == currentSlot && currentItem == this.itemToRender);

        if (this.itemToRender == null && currentItem == null) {
            sameItem = true;
        }

        // If the player is on the same hotbar slot, update itemToRender without triggering slot re-equip
        if (currentItem != null && this.itemToRender != null && currentItem != this.itemToRender) {
            if (this.equippedItemSlot == currentSlot) {
                this.itemToRender = currentItem;
                sameItem = true;
            } else if (currentItem.getItem() == this.itemToRender.getItem()
                && currentItem.getItemDamage() == this.itemToRender.getItemDamage()
                && ItemStack.areItemStackTagsEqual(currentItem, this.itemToRender)) {
                    this.itemToRender = currentItem;
                    sameItem = true;
                }
        }

        float targetProgress = 0.0F;
        if (sameItem) {
            if (Config.enableAttackCooldown && Config.enableItemReequipAnimation && player instanceof ICombatPlayer) {
                float charge = ((ICombatPlayer) player).getCooledAttackStrength(1.0F);
                targetProgress = charge * charge * charge;
            } else {
                targetProgress = 1.0F;
            }
        }

        float step = 0.4F;
        float diff = targetProgress - this.equippedProgress;
        diff = MathHelper.clamp_float(diff, -step, step);
        this.equippedProgress += diff;

        if (this.equippedProgress < 0.1F) {
            this.itemToRender = currentItem;
            this.equippedItemSlot = currentSlot;
        }

        ci.cancel();
    }
}
