package logisticspipes.network.guis.module.inpipe;

import java.io.IOException;

import logisticspipes.gui.modules.GuiAdvancedExtractor;
import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.utils.gui.DummyContainer;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
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
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(areItemsIncluded);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		areItemsIncluded = data.readBoolean();
	}
}
