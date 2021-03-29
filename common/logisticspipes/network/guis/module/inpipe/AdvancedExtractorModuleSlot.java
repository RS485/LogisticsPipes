package logisticspipes.network.guis.module.inpipe;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.gui.modules.GuiAdvancedExtractor;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;
import network.rs485.logisticspipes.module.AsyncAdvancedExtractor;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class AdvancedExtractorModuleSlot extends ModuleCoordinatesGuiProvider {

	private boolean areItemsIncluded;

	public AdvancedExtractorModuleSlot(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		AsyncAdvancedExtractor module = this.getLogisticsModule(player.getEntityWorld(), AsyncAdvancedExtractor.class);
		if (module == null) {
			return null;
		}
		module.getItemsIncluded().setValue(areItemsIncluded);
		return new GuiAdvancedExtractor(player.inventory, module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		return SimpleFilterInventorySlot.getContainerFromFilterModule(this, player);
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

	public boolean isAreItemsIncluded() {
		return this.areItemsIncluded;
	}

	public AdvancedExtractorModuleSlot setAreItemsIncluded(boolean areItemsIncluded) {
		this.areItemsIncluded = areItemsIncluded;
		return this;
	}
}
