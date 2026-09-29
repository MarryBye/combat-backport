package com.marrybye.combatbackport.network;

import net.minecraft.client.Minecraft;

import com.marrybye.combatbackport.api.ICombatPlayer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class PacketShieldCooldown implements IMessage {

    private int ticks;

    public PacketShieldCooldown() {}

    public PacketShieldCooldown(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.ticks = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.ticks);
    }

    public static class Handler implements IMessageHandler<PacketShieldCooldown, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketShieldCooldown message, MessageContext ctx) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer instanceof ICombatPlayer) {
                ((ICombatPlayer) mc.thePlayer).setShieldCooldown(message.ticks);
            }
            return null;
        }
    }
}
