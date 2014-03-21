package logisticspipes.network.abstractpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.INBTPacketProvider;
import logisticspipes.network.SendNBTTagCompound;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.nbt.NBTTagCompound;

@Accessors(chain=true)
public abstract class NBTCoordinatesPacket extends CoordinatesPacket {
	
	@Getter
	@Setter
	private NBTTagCompound tag;
	
	public NBTCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		SendNBTTagCompound.writeNBTTagCompound(tag, data);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		tag = SendNBTTagCompound.readNBTTagCompound(data);
	}
	
	public NBTCoordinatesPacket readFromProvider(INBTPacketProvider provider) {
		tag = new NBTTagCompound("tag");
		provider.writeToPacketNBT(tag);
		return this;
	}
}
