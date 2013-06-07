package logisticspipes.network.oldpackets;

import java.util.LinkedList;

import logisticspipes.network.NetworkConstants;
import logisticspipes.utils.ItemMessage;




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
