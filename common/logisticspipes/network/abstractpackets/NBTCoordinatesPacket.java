package logisticspipes.network.abstractpackets;

import java.io.IOException;

import logisticspipes.network.INBTPacketProvider;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
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
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeNBTTagCompound(tag);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		tag = data.readNBTTagCompound();
	}
	
	public NBTCoordinatesPacket readFromProvider(INBTPacketProvider provider) {
		tag = new NBTTagCompound();
		provider.writeToPacketNBT(tag);
		return this;
	}
}
