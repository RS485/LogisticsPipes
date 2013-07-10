package logisticspipes.network.abstractpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.SendNBTTagCompound;
import logisticspipes.network.packets.orderer.RequestSubmitPacket;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.NBTTagCompound;

public abstract class RequestPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private ItemIdentifierStack stack;

	@Getter
	@Setter
	private int dimension;

	public RequestPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new RequestSubmitPacket(getId());
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(getStack().getItem().itemID);
		data.writeInt(getStack().getItem().itemDamage);
		data.writeInt(getStack().stackSize);
		SendNBTTagCompound.writeNBTTagCompound(getStack().getItem().tag, data);
		data.writeInt(dimension);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		int itemID = data.readInt();
		int dataValue = data.readInt();
		int amount = data.readInt();
		NBTTagCompound tag = SendNBTTagCompound.readNBTTagCompound(data);
		setStack(ItemIdentifier.get(itemID, dataValue, tag).makeStack(amount));
		dimension = data.readInt();
	}
}
