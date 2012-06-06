package net.minecraft.src.buildcraft.krapht.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.core.network.BuildCraftPacket;
import net.minecraft.src.krapht.ItemIdentifier;

public class PacketRequestGuiContent extends LogisticsPipesPacket {

	public HashMap<ItemIdentifier, Integer> _availableItems = new HashMap<ItemIdentifier, Integer>();
	public LinkedList<ItemIdentifier> _craftableItems = new LinkedList<ItemIdentifier>();
	public LinkedList<ItemIdentifier> _allItems = new LinkedList<ItemIdentifier>();
	
	public PacketRequestGuiContent() {
		super();
	}
	
	public PacketRequestGuiContent(HashMap<ItemIdentifier, Integer> availableItems, LinkedList<ItemIdentifier> craftableItems, LinkedList<ItemIdentifier> allItems) {
		super();
		_availableItems = availableItems;
		_craftableItems = craftableItems;
		_allItems = allItems;
	}
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		for(ItemIdentifier item:_availableItems.keySet()) {
			data.write(1); //byte
			data.writeInt(item.itemID);
			data.writeInt(item.itemDamage);
			data.writeInt(_availableItems.get(item));
		}
		data.write(0); //end
		for(ItemIdentifier item:_craftableItems) {
			data.write(1); //byte
			data.writeInt(item.itemID);
			data.writeInt(item.itemDamage);
		}
		data.write(0); //end
		for(ItemIdentifier item:_allItems) {
			data.write(1); //byte
			data.writeInt(item.itemID);
			data.writeInt(item.itemDamage);
		}
		data.write(0); //end
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		while(data.read() != 0) { // read until the end
			int itemID = data.readInt();
			int dataValue = data.readInt();
			int amount = data.readInt();
			_availableItems.put(ItemIdentifier.get(itemID, dataValue), amount);
		}
		while(data.read() != 0) { // read until the end
			int itemID = data.readInt();
			int dataValue = data.readInt();
			_craftableItems.add(ItemIdentifier.get(itemID, dataValue));
		}
		while(data.read() != 0) { // read until the end
			int itemID = data.readInt();
			int dataValue = data.readInt();
			_allItems.add(ItemIdentifier.get(itemID, dataValue));
		}
	}

	@Override
	public int getID() {
		return NetworkConstants.ORDERER_CONTENT_ANSWER;
	}

}
