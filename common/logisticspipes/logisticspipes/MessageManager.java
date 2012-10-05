package logisticspipes.logisticspipes;

import java.util.LinkedList;

import logisticspipes.network.packets.PacketCraftingLoop;
import logisticspipes.network.packets.PacketItems;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemMessage;
import net.minecraft.src.EntityPlayer;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class MessageManager {
	
	public static void overflow(EntityPlayer player, ItemIdentifier item) {
		LinkedList<ItemMessage> error = new LinkedList<ItemMessage>();
		error.add(new ItemMessage(item.itemID, item.itemDamage, 1, item.tag));
		PacketDispatcher.sendPacketToPlayer(new PacketCraftingLoop(error).getPacket(), (Player)player);
	}

	public static void errors(EntityPlayer player, LinkedList<ItemMessage> errors) {
		PacketDispatcher.sendPacketToPlayer(new PacketItems(errors,true).getPacket(), (Player)player);
	}

	public static void requested(EntityPlayer player, LinkedList<ItemMessage> items) {
		PacketDispatcher.sendPacketToPlayer(new PacketItems(items,false).getPacket(), (Player)player);
	}
	
}
