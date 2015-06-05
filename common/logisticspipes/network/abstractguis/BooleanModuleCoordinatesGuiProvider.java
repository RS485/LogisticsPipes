package logisticspipes.network.abstractguis;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public abstract class BooleanModuleCoordinatesGuiProvider extends ModuleCoordinatesGuiProvider {

	public BooleanModuleCoordinatesGuiProvider(int id) {
		super(id);
	}

	@Getter
	@Setter
	private boolean flag;

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(flag);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		flag = data.readBoolean();
	}
}
