package logisticspipes.network.abstractguis;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain=true)
public abstract class ModuleCoordinatesGuiProvider extends CoordinatesGuiProvider {
	
	public ModuleCoordinatesGuiProvider(int id) {
		super(id);
	}

	@Getter
	@Setter
	private int slot;

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(slot);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		slot = data.readInt();
	}
}
