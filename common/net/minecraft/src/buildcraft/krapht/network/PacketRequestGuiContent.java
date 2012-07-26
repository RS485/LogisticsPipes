package net.minecraft.src.buildcraft.krapht.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.krapht.ItemIdentifier;

public class PacketRequestGuiContent extends LogisticsPipesPacket {

	public HashMap<ItemIdentifier, Integer> _availableItems = new HashMap<ItemIdentifier, Integer>();
	public LinkedList<ItemIdentifier> _craftableItems = new LinkedList<ItemIdentifier>();
	public LinkedList<ItemIdentifier> _allItems = new LinkedList<ItemIdentifier>();

	public PacketRequestGuiContent() {
		super();
	}

	public PacketRequestGuiContent(HashMap<ItemIdentifier, Integer> availableItems, LinkedList<ItemIdentifier> craftableItems,
			LinkedList<ItemIdentifier> allItems) {
		super();
		_availableItems = availableItems;
		_craftableItems = craftableItems;
		_allItems = allItems;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		for (final ItemIdentifier item : _availableItems.keySet()) {
			data.write(1); // byte
			data.writeInt(item.itemID);
			data.writeInt(item.itemDamage);
			data.writeInt(_availableItems.get(item));
			SendNBTTagCompound.writeNBTTagCompound(item.tag, data);
		}
		data.write(0); // end
		for (final ItemIdentifier item : _craftableItems) {
			data.write(1); // byte
			data.writeInt(item.itemID);
			data.writeInt(item.itemDamage);
			SendNBTTagCompound.writeNBTTagCompound(item.tag, data);
		}
		data.write(0); // end
		for (final ItemIdentifier item : _allItems) {
			data.write(1); // byte
			data.writeInt(item.itemID);
			data.writeInt(item.itemDamage);
			SendNBTTagCompound.writeNBTTagCompound(item.tag, data);
		}
		data.write(0); // end
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		while (data.read() != 0) { // read until the end
			final int itemID = data.readInt();
			final int dataValue = data.readInt();
			final int amount = data.readInt();
			final NBTTagCompound tag = SendNBTTagCompound.readNBTTagCompound(data);
			_availableItems.put(ItemIdentifier.get(itemID, dataValue, tag), amount);
		}
		while (data.read() != 0) { // read until the end
			final int itemID = data.readInt();
			final int dataValue = data.readInt();
			final NBTTagCompound tag = SendNBTTagCompound.readNBTTagCompound(data);
			_craftableItems.add(ItemIdentifier.get(itemID, dataValue, tag));
		}
		while (data.read() != 0) { // read until the end
			final int itemID = data.readInt();
			final int dataValue = data.readInt();
			final NBTTagCompound tag = SendNBTTagCompound.readNBTTagCompound(data);
			_allItems.add(ItemIdentifier.get(itemID, dataValue, tag));
		}
	}

	@Override
	public int getID() {
		return NetworkConstants.ORDERER_CONTENT_ANSWER;
	}

}
