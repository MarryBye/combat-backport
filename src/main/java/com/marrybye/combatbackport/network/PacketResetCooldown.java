package com.marrybye.combatbackport.network;

import net.minecraft.entity.player.EntityPlayerMP;

import com.marrybye.combatbackport.api.ICombatPlayer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketResetCooldown implements IMessage {

    public PacketResetCooldown() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<PacketResetCooldown, IMessage> {

        @Override
        public IMessage onMessage(PacketResetCooldown message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player instanceof ICombatPlayer) {
                ((ICombatPlayer) player).resetAttackCooldown();
            }
            return null;
        }
    }
}
