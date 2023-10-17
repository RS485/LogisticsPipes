package logisticspipes.network.abstractguis;

import lombok.Getter;
import lombok.Setter;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class BooleanModuleCoordinatesGuiProvider extends ModuleCoordinatesGuiProvider {

	@Getter
	@Setter
	private boolean flag;

	public BooleanModuleCoordinatesGuiProvider(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeBoolean(flag);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		flag = input.readBoolean();
	}
}
