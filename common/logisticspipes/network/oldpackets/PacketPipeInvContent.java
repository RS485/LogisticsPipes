package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import logisticspipes.network.SendNBTTagCompound;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import net.minecraft.nbt.NBTTagCompound;

public class PacketPipeInvContent extends PacketCoordinates {

	public Collection<ItemIdentifierStack> _allItems = new LinkedList<ItemIdentifierStack>();

	public PacketPipeInvContent() {
		super();
	}

	public PacketPipeInvContent(int id, int x, int y, int z, Collection<ItemIdentifierStack> allItems) {
		super(id, x, y, z);
		_allItems = allItems;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		for (final ItemIdentifierStack item : _allItems) {
			data.write(1); // byte
			if(item == null) {
				data.writeInt(0);
			} else {
				data.writeInt(item.getItem().itemID);
				data.writeInt(item.getItem().itemDamage);
				data.writeInt(item.stackSize);
				SendNBTTagCompound.writeNBTTagCompound(item.getItem().tag, data);
			}
		}
		data.write(0); // end
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		while (data.read() != 0) { // read until the end
			final int itemID = data.readInt();
			if(itemID != 0) {
				final int dataValue = data.readInt();
				final int amount = data.readInt();
				final NBTTagCompound tag = SendNBTTagCompound.readNBTTagCompound(data);
				_allItems.add(ItemIdentifier.get(itemID, dataValue, tag).makeStack(amount));
			} else {
				_allItems.add(null);
			}
		}
	}
}
