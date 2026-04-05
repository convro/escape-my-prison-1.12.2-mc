package com.prisonbreakmod.network;

import com.prisonbreakmod.PrisonBreakMod;
import com.prisonbreakmod.crafting.ZbyszekCrafting;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/** Simple packet handler for client-server communication. */
public class PrisonPacketHandler {

    private static SimpleNetworkWrapper INSTANCE;

    public static void init() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(PrisonBreakMod.MODID);
        INSTANCE.registerMessage(ZbyszekCraftHandler.class, ZbyszekCraftPacket.class, 0, Side.SERVER);
        INSTANCE.registerMessage(OpenDialogueHandler.class, OpenDialoguePacket.class, 1, Side.SERVER);
    }

    public static void sendZbyszekCraftRequest(String recipeId) {
        INSTANCE.sendToServer(new ZbyszekCraftPacket(recipeId));
    }

    // -------------------------------------------------------------------------
    // Zbyszek Craft Packet
    // -------------------------------------------------------------------------

    public static class ZbyszekCraftPacket implements IMessage {
        private String recipeId = "";

        public ZbyszekCraftPacket() {}
        public ZbyszekCraftPacket(String recipeId) { this.recipeId = recipeId; }

        @Override
        public void fromBytes(ByteBuf buf) {
            int len = buf.readInt();
            byte[] bytes = new byte[len];
            buf.readBytes(bytes);
            recipeId = new String(bytes);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            byte[] bytes = recipeId.getBytes();
            buf.writeInt(bytes.length);
            buf.writeBytes(bytes);
        }
    }

    public static class ZbyszekCraftHandler implements IMessageHandler<ZbyszekCraftPacket, IMessage> {
        @Override
        public IMessage onMessage(ZbyszekCraftPacket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            net.minecraftforge.fml.common.FMLCommonHandler.instance()
                    .getMinecraftServerInstance().addScheduledTask(() ->
                            ZbyszekCrafting.startCraft(message.recipeId, player, player.world));
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Open Dialogue Packet (client requests opening dialogue with NPC)
    // -------------------------------------------------------------------------

    public static class OpenDialoguePacket implements IMessage {
        private int entityId;

        public OpenDialoguePacket() {}
        public OpenDialoguePacket(int entityId) { this.entityId = entityId; }

        @Override
        public void fromBytes(ByteBuf buf) { entityId = buf.readInt(); }

        @Override
        public void toBytes(ByteBuf buf) { buf.writeInt(entityId); }
    }

    public static class OpenDialogueHandler implements IMessageHandler<OpenDialoguePacket, IMessage> {
        @Override
        public IMessage onMessage(OpenDialoguePacket message, MessageContext ctx) {
            // Server validates and then sends response to client
            // For simplicity, dialogue is purely client-side after entity info is fetched
            return null;
        }
    }
}
