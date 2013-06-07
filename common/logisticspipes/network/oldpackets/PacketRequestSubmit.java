package logisticspipes.network.oldpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.NetworkConstants;
import logisticspipes.network.SendNBTTagCompound;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.nbt.NBTTagCompound;

public class PacketRequestSubmit extends PacketCoordinates {

	public int itemID;
	public int dataValue;
	public int amount;
	public NBTTagCompound tag;
	public int dimension;

	public PacketRequestSubmit() {
		super();
	}

	public PacketRequestSubmit(int x, int y, int z, int dim, ItemIdentifier selectedItem, int amount) {
		this(x, y, z, dim, selectedItem, amount, NetworkConstants.REQUEST_SUBMIT);
	}

	public PacketRequestSubmit(int x, int y, int z, int dim, ItemIdentifier selectedItem, int amount, int id) {
		super(id, x, y, z);
		itemID = selectedItem.itemID;
		dataValue = selectedItem.itemDamage;
		tag = selectedItem.tag;
		this.amount = amount;
		this.dimension = dim;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(itemID);
		data.writeInt(dataValue);
		data.writeInt(amount);
		SendNBTTagCompound.writeNBTTagCompound(tag, data);
		data.writeInt(dimension);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		itemID = data.readInt();
		dataValue = data.readInt();
		amount = data.readInt();
		tag = SendNBTTagCompound.readNBTTagCompound(data);
		dimension = data.readInt();
	}
}
