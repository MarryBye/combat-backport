package com.marrybye.combatbackport.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.util.MovingObjectPosition;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.marrybye.combatbackport.Config;
import com.marrybye.combatbackport.api.ICombatPlayer;
import com.marrybye.combatbackport.client.ClientMiningHandler;
import com.marrybye.combatbackport.network.CombatPacketHandler;
import com.marrybye.combatbackport.network.PacketResetCooldown;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    public EntityClientPlayerMP thePlayer;

    @Shadow
    public MovingObjectPosition objectMouseOver;

    @Inject(method = "func_147116_af", at = @At("HEAD"))
    private void combatbackport$onLeftClick(CallbackInfo ci) {
        if (!Config.enableAttackCooldown || this.thePlayer == null || !(this.thePlayer instanceof ICombatPlayer)) {
            return;
        }

        if (this.objectMouseOver != null
            && this.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            // Cooldown is evaluated during attackTargetEntityWithCurrentItem and reset at @At("RETURN")
            return;
        }

        if (this.objectMouseOver != null
            && this.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            ClientMiningHandler.onBlockInteracted();
            return;
        }

        // Only reset cooldown on missed swing (air) if air swing cooldown is enabled,
        // and only if the player didn't recently interact with or destroy a block (e.g. clearing foliage/grass).
        if (this.objectMouseOver == null
            || this.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) {
            if (Config.enableAirSwingCooldown && !ClientMiningHandler.isRecentlyInteractedWithBlock()) {
                ((ICombatPlayer) this.thePlayer).resetAttackCooldown();
                CombatPacketHandler.INSTANCE.sendToServer(new PacketResetCooldown());
            }
        }
    }
}
