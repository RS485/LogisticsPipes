package logisticspipes.network.abstractpackets;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.packets.orderer.RequestSubmitPacket;
import logisticspipes.utils.item.ItemIdentifierStack;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
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
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeItemIdentifierStack(stack);
		data.writeInt(dimension);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		stack = data.readItemIdentifierStack();
		dimension = data.readInt();
	}
}
