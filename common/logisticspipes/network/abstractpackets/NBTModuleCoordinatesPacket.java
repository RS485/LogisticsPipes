package logisticspipes.network.abstractpackets;

import java.io.IOException;

import net.minecraft.nbt.NBTTagCompound;

import lombok.Getter;
import lombok.Setter;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class NBTModuleCoordinatesPacket extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private NBTTagCompound tag;

	public NBTModuleCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		output.writeNBTTagCompound(tag);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		tag = input.readNBTTagCompound();
	}
}
