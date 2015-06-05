package logisticspipes.network.abstractpackets;

import java.io.IOException;
import java.util.List;

import logisticspipes.network.IReadListObject;
import logisticspipes.network.IWriteListObject;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
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
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeList(list, this);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		list = data.readList(this);
	}

	@Override
	public abstract ListSyncPacket<E> template();
}
