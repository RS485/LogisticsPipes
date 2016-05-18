package logisticspipes.network.guis.module.inpipe;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.gui.modules.GuiApiaristAnalyser;
import logisticspipes.modules.ModuleApiaristAnalyser;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.utils.gui.DummyContainer;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class ApiaristAnalyzerModuleSlot extends ModuleCoordinatesGuiProvider {

	@Getter
	@Setter
	private int extractorMode;

	public ApiaristAnalyzerModuleSlot(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		ModuleApiaristAnalyser module = this.getLogisticsModule(player.getEntityWorld(), ModuleApiaristAnalyser.class);
		if (module == null) {
			return null;
		}
		module.setExtractMode(extractorMode);
		return new GuiApiaristAnalyser(module, player.inventory);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		ModuleApiaristAnalyser module = this.getLogisticsModule(player.getEntityWorld(), ModuleApiaristAnalyser.class);
		if (module == null) {
			return null;
		}
		return new DummyContainer(player.inventory, null);
	}

	@Override
	public GuiProvider template() {
		return new ApiaristAnalyzerModuleSlot(getId());
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeInt(extractorMode);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		extractorMode = input.readInt();
	}
}
