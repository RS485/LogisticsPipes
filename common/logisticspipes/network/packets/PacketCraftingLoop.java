package logisticspipes.network.packets;

import java.util.LinkedList;

import logisticspipes.main.ItemMessage;
import logisticspipes.network.NetworkConstants;




public class PacketCraftingLoop extends PacketItems {

	public PacketCraftingLoop() {
		super();
	}

	public PacketCraftingLoop(LinkedList<ItemMessage> error) {
		super(error);
	}

	@Override
	public int getID() {
		return NetworkConstants.CRAFTING_LOOP;
	}
}
