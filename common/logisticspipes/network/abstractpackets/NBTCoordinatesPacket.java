package logisticspipes.network.abstractpackets;

import net.minecraft.nbt.CompoundTag;

import lombok.Getter;
import lombok.Setter;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class NBTCoordinatesPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private CompoundTag tag;

	public NBTCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeNBTTagCompound(tag);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		tag = input.readNBTTagCompound();
	}
}
