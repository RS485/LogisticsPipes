package logisticspipes.buildcraft.logisticspipes;

import java.util.LinkedList;

import logisticspipes.buildcraft.krapht.ItemMessage;
import logisticspipes.buildcraft.krapht.network.PacketCraftingLoop;
import logisticspipes.buildcraft.krapht.network.PacketItems;
import logisticspipes.krapht.ItemIdentifier;


import net.minecraft.src.EntityPlayer;
import cpw.mods.fml.common.network.PacketDispatcher;

public class MessageManager {
	
	public static void overflow(EntityPlayer player, ItemIdentifier item) {
		LinkedList<ItemMessage> error = new LinkedList<ItemMessage>();
		error.add(new ItemMessage(item.itemID, item.itemDamage, 1, item.tag));
		PacketDispatcher.sendPacketToPlayer(new PacketCraftingLoop(error).getPacket(), player);
	}

	public static void errors(EntityPlayer player, LinkedList<ItemMessage> errors) {
		PacketDispatcher.sendPacketToPlayer(new PacketItems(errors,true).getPacket(), player);
	}

	public static void requested(EntityPlayer player, LinkedList<ItemMessage> items) {
		PacketDispatcher.sendPacketToPlayer(new PacketItems(items,false).getPacket(), player);
	}
	
}
