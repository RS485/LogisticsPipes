package logisticspipes.network.abstractpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.packets.orderer.RequestSubmitPacket;
import logisticspipes.utils.item.ItemIdentifierStack;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain=true)
public abstract class RequestPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private ItemIdentifierStack[] stacks;

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
		
		data.writeShort(stacks.length);
		
		for (ItemIdentifierStack stack : stacks){
			stack.write(data);
		}
		
		data.writeInt(dimension);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		
		ItemIdentifierStack[] stacks = new ItemIdentifierStack[data.readUnsignedShort()];
		
		for (int i = 0; i < stacks.length; i++){
			stacks[i] = ItemIdentifierStack.read(data);
		}
		
		this.stacks = stacks;
		
		dimension = data.readInt();
	}
}
