package logisticspipes.logisticspipes;

import java.util.LinkedList;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.ComponentList;
import logisticspipes.network.packets.orderer.CraftingLoop;
import logisticspipes.network.packets.orderer.MissingItems;
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
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CraftingLoop.class).setStack(new ItemStack(error.get(0).id, error.get(0).data, error.get(0).amount)), (Player)player);
		//MainProxy.sendPacketToPlayer(new PacketCraftingLoop(error), (Player)player);
	}

	public static void errors(EntityPlayer player, LinkedList<ItemMessage> errors) {
//TODO 	MainProxy.sendPacketToPlayer(new PacketItems(NetworkConstants.MISSING_ITEMS, errors,true).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(errors).setFlag(true), (Player)player);
	}

	public static void requested(EntityPlayer player, LinkedList<ItemMessage> items) {
//TODO	MainProxy.sendPacketToPlayer(new PacketItems(NetworkConstants.MISSING_ITEMS, items,false).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(items).setFlag(false), (Player)player);
	}

	public static void simulated(EntityPlayer player, LinkedList<ItemMessage> used, LinkedList<ItemMessage> missing) {
//TODO 	MainProxy.sendPacketToPlayer(new PacketSimulate(NetworkConstants.COMPONENT_LIST, used,missing).getPacket(), (Player)player);		
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ComponentList.class).setUsed(used).setMissing(missing), (Player)player);
	}
}
