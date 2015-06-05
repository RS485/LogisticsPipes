package logisticspipes.network.abstractpackets;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

import net.minecraft.nbt.NBTTagCompound;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
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
}
