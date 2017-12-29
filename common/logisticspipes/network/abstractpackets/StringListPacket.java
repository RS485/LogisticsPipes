package logisticspipes.network.abstractpackets;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class StringListPacket extends ModernPacket {

	@Getter
	@Setter
	private List<String> stringList = new ArrayList<>();

	public StringListPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		int size = input.readInt();
		for (int i = 0; i < size; i++) {
			getStringList().add(input.readUTF());
		}
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeInt(getStringList().size());
		for (int i = 0; i < getStringList().size(); i++) {
			output.writeUTF(getStringList().get(i));
		}
	}
}
