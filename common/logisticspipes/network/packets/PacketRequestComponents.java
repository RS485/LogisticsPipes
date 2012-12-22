package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.NetworkConstants;
import logisticspipes.network.SendNBTTagCompound;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.nbt.NBTTagCompound;


public class PacketRequestComponents extends PacketCoordinates {
	
	public int itemID;
	public int dataValue;
	public NBTTagCompound tag;
	public int dimension;

	public PacketRequestComponents() {
		super();
	}

	public PacketRequestComponents(int x, int y, int z, int dim, ItemIdentifier selectedItem) {
		super(NetworkConstants.REQUEST_COMPONENTS, x, y, z);
		itemID = selectedItem.itemID;
		dataValue = selectedItem.itemDamage;
		tag = selectedItem.tag;
		this.dimension = dim;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(itemID);
		data.writeInt(dataValue);
		SendNBTTagCompound.writeNBTTagCompound(tag, data);
		data.writeInt(dimension);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		itemID = data.readInt();
		dataValue = data.readInt();
		tag = SendNBTTagCompound.readNBTTagCompound(data);
		dimension = data.readInt();
	}
}
