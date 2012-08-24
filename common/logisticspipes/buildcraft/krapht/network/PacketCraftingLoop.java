package logisticspipes.buildcraft.krapht.network;

import java.util.LinkedList;

import logisticspipes.buildcraft.krapht.ItemMessage;




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
