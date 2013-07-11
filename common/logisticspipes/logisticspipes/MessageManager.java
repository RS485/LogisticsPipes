package logisticspipes.logisticspipes;

import java.util.LinkedList;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.oldpackets.PacketItems;
import logisticspipes.network.oldpackets.PacketSimulate;
import logisticspipes.network.packets.orderer.CraftingLoop;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.Player;

public class MessageManager {
	
	public static void overflow(EntityPlayer player, ItemIdentifier item) {
		LinkedList<ItemMessage> error = new LinkedList<ItemMessage>();
		error.add(new ItemMessage(item.itemID, item.itemDamage, 1, item.tag));
//TODO Must be handled manualy
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CraftingLoop.class).setStack(new ItemStack(error.get(0).id, error.get(0).data, error.get(0).amount)).getPacket(), (Player)player);
		//MainProxy.sendPacketToPlayer(new PacketCraftingLoop(error).getPacket(), (Player)player);
	}

	public static void errors(EntityPlayer player, LinkedList<ItemMessage> errors) {
//TODO Must be handled manualy
		MainProxy.sendPacketToPlayer(new PacketItems(errors,true).getPacket(), (Player)player);
	}

	public static void requested(EntityPlayer player, LinkedList<ItemMessage> items) {
//TODO Must be handled manualy
		MainProxy.sendPacketToPlayer(new PacketItems(items,false).getPacket(), (Player)player);
	}

	public static void simulated(EntityPlayer player, LinkedList<ItemMessage> used, LinkedList<ItemMessage> missing) {
//TODO Must be handled manualy
		MainProxy.sendPacketToPlayer(new PacketSimulate(used,missing).getPacket(), (Player)player);		
	}
	
}
