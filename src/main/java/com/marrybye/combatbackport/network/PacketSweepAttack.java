package com.marrybye.combatbackport.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class PacketSweepAttack implements IMessage {

    public double x;
    public double y;
    public double z;
    public float yaw;

    public PacketSweepAttack() {}

    public PacketSweepAttack(double x, double y, double z, float yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.yaw = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeFloat(this.yaw);
    }

    public static class Handler implements IMessageHandler<PacketSweepAttack, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketSweepAttack message, MessageContext ctx) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
            if (mc.theWorld != null) {
                com.marrybye.combatbackport.client.particle.EntitySweepFX fx = new com.marrybye.combatbackport.client.particle.EntitySweepFX(
                    mc.theWorld,
                    message.x,
                    message.y,
                    message.z,
                    message.yaw);
                mc.effectRenderer.addEffect(fx);
            }
            return null;
        }
    }
}
