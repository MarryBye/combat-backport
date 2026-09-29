package com.marrybye.combatbackport.mixins;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.marrybye.combatbackport.Config;
import com.marrybye.combatbackport.combat.WeaponRegistry;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Shadow
    public abstract Item getItem();

    @Inject(method = "getAttributeModifiers", at = @At("RETURN"), cancellable = true)
    private void combatbackport$getAttributeModifiers(CallbackInfoReturnable<Multimap> cir) {
        if (!Config.enableWeaponDamageRebalance) {
            return;
        }

        Item item = this.getItem();
        if (item == null || !WeaponRegistry.isRebalanceEligible(item)) {
            return;
        }

        Multimap original = cir.getReturnValue();
        if (original == null) {
            return;
        }

        String key = SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName();
        if (original.containsKey(key)) {
            double rebalanced = WeaponRegistry.getRebalancedDamage(item, (ItemStack) (Object) this);
            if (rebalanced >= 0) {
                Multimap copy = HashMultimap.create(original);
                copy.removeAll(key);
                copy.put(key, new AttributeModifier(WeaponRegistry.ITEM_MODIFIER_UUID, "Tool modifier", rebalanced, 0));
                cir.setReturnValue(copy);
            }
        }
    }
}
