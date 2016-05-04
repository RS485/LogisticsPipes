package logisticspipes.network.abstractguis;

import java.io.IOException;

import lombok.Getter;
import lombok.Setter;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class BooleanModuleCoordinatesGuiProvider extends ModuleCoordinatesGuiProvider {

	public BooleanModuleCoordinatesGuiProvider(int id) {
		super(id);
	}

	@Getter
	@Setter
	private boolean flag;

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		output.writeBoolean(flag);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		flag = input.readBoolean();
	}
}
