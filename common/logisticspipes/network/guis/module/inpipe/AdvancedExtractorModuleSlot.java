package logisticspipes.network.guis.module.inpipe;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.gui.modules.GuiAdvancedExtractor;
import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.utils.gui.DummyContainer;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

import logisticspipes.utils.StaticResolve;

@StaticResolve
public class AdvancedExtractorModuleSlot extends ModuleCoordinatesGuiProvider {

	@Getter
	@Setter
	private boolean areItemsIncluded;

	public AdvancedExtractorModuleSlot(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		ModuleAdvancedExtractor module = this.getLogisticsModule(player.getEntityWorld(), ModuleAdvancedExtractor.class);
		if (module == null) {
			return null;
		}
		module.setItemsIncluded(areItemsIncluded);
		return new GuiAdvancedExtractor(player.inventory, module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		ModuleAdvancedExtractor module = this.getLogisticsModule(player.getEntityWorld(), ModuleAdvancedExtractor.class);
		if (module == null) {
			return null;
		}
		DummyContainer dummy = new DummyContainer(player.inventory, module.getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(8, 60);
		//Pipe slots
		for (int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
			dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
		}
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new AdvancedExtractorModuleSlot(getId());
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeBoolean(areItemsIncluded);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		areItemsIncluded = input.readBoolean();
	}
}
