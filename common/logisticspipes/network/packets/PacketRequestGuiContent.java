package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import logisticspipes.network.LogisticsPipesPacket;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.SendNBTTagCompound;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import net.minecraft.src.NBTTagCompound;

public class PacketRequestGuiContent extends LogisticsPipesPacket {

	public LinkedList<ItemIdentifierStack> _allItems = new LinkedList<ItemIdentifierStack>();
	private int id;

	public PacketRequestGuiContent() {
		super();
	}

	public PacketRequestGuiContent(LinkedList<ItemIdentifierStack> allItems) {
		super();
		_allItems = allItems;
		id = NetworkConstants.ORDERER_CONTENT_ANSWER;
	}

	public PacketRequestGuiContent(LinkedList<ItemIdentifierStack> allItems, int id) {
		super();
		_allItems = allItems;
		this.id = id;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		for (final ItemIdentifierStack item : _allItems) {
			data.write(1); // byte
			data.writeInt(item.getItem().itemID);
			data.writeInt(item.getItem().itemDamage);
			data.writeInt(item.stackSize);
			SendNBTTagCompound.writeNBTTagCompound(item.getItem().tag, data);
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
			_allItems.add(ItemIdentifier.get(itemID, dataValue, tag).makeStack(amount));
		}
	}

	@Override
	public int getID() {
		return id;
	}

}
