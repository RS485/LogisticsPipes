package logisticspipes.logisticspipes;

import java.util.LinkedList;

import logisticspipes.network.oldpackets.PacketCraftingLoop;
import logisticspipes.network.oldpackets.PacketItems;
import logisticspipes.network.oldpackets.PacketSimulate;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.common.network.Player;

public class MessageManager {
	
	public static void overflow(EntityPlayer player, ItemIdentifier item) {
		LinkedList<ItemMessage> error = new LinkedList<ItemMessage>();
		error.add(new ItemMessage(item.itemID, item.itemDamage, 1, item.tag));
		MainProxy.sendPacketToPlayer(new PacketCraftingLoop(error).getPacket(), (Player)player);
	}

	public static void errors(EntityPlayer player, LinkedList<ItemMessage> errors) {
		MainProxy.sendPacketToPlayer(new PacketItems(errors,true).getPacket(), (Player)player);
	}

	public static void requested(EntityPlayer player, LinkedList<ItemMessage> items) {
		MainProxy.sendPacketToPlayer(new PacketItems(items,false).getPacket(), (Player)player);
	}

	public static void simulated(EntityPlayerMP player, LinkedList<ItemMessage> used, LinkedList<ItemMessage> missing) {
		MainProxy.sendPacketToPlayer(new PacketSimulate(used,missing).getPacket(), (Player)player);		
	}
	
}
