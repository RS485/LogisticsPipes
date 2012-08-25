package logisticspipes.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.utils.ItemIdentifier;

import net.minecraft.src.CompressedStreamTools;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet;

public class PacketRequestSubmit extends PacketCoordinates {

	public int itemID;
	public int dataValue;
	public int amount;
	public NBTTagCompound tag;

	public PacketRequestSubmit() {
		super();
	}

	public PacketRequestSubmit(int x, int y, int z, ItemIdentifier selectedItem, int amount) {
		super(NetworkConstants.REQUEST_SUBMIT, x, y, z);
		itemID = selectedItem.itemID;
		dataValue = selectedItem.itemDamage;
		tag = selectedItem.tag;
		this.amount = amount;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(itemID);
		data.writeInt(dataValue);
		data.writeInt(amount);
		SendNBTTagCompound.writeNBTTagCompound(tag, data);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		itemID = data.readInt();
		dataValue = data.readInt();
		amount = data.readInt();
		tag = SendNBTTagCompound.readNBTTagCompound(data);
	}
}
