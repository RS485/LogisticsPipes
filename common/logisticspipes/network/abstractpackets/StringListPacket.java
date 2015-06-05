package logisticspipes.network.abstractpackets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public abstract class StringListPacket extends ModernPacket {

	@Getter
	@Setter
	private List<String> stringList = new ArrayList<String>();

	public StringListPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		int size = data.readInt();
		for (int i = 0; i < size; i++) {
			getStringList().add(data.readUTF());
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeInt(getStringList().size());
		for (int i = 0; i < getStringList().size(); i++) {
			data.writeUTF(getStringList().get(i));
		}
	}
}
