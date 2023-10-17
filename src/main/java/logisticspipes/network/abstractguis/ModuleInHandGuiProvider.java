package logisticspipes.network.abstractguis;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class ModuleInHandGuiProvider extends GuiProvider {

	private int invSlot;

	public ModuleInHandGuiProvider(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeInt(invSlot);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		invSlot = input.readInt();
	}

	public int getInvSlot() {
		return this.invSlot;
	}

	public ModuleInHandGuiProvider setInvSlot(int invSlot) {
		this.invSlot = invSlot;
		return this;
	}
}
