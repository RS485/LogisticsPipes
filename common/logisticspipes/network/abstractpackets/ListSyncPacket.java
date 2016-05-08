package logisticspipes.network.abstractpackets;

import java.io.IOException;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.IReadListObject;
import logisticspipes.network.IWriteListObject;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class ListSyncPacket<E> extends CoordinatesPacket implements IWriteListObject<E>, IReadListObject<E> {

	@Setter
	@Getter(value = AccessLevel.PROTECTED)
	private List<E> list;

	public ListSyncPacket(int id, int x, int y, int z) {
		super(id);
		setPosX(x);
		setPosY(y);
		setPosZ(z);
	}

	public ListSyncPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		output.writeCollection(list, this);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		list = input.readArrayList(this);
	}

	@Override
	public abstract ListSyncPacket<E> template();
}
