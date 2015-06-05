package logisticspipes.network.guis.module.inpipe;

import logisticspipes.gui.modules.GuiElectricManager;
import logisticspipes.modules.ModuleElectricManager;
import logisticspipes.network.abstractguis.BooleanModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.utils.gui.DummyContainer;

import net.minecraft.entity.player.EntityPlayer;

public class ElectricModuleSlot extends BooleanModuleCoordinatesGuiProvider {

	public ElectricModuleSlot(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		ModuleElectricManager module = this.getLogisticsModule(player.getEntityWorld(), ModuleElectricManager.class);
		if (module == null) {
			return null;
		}
		module.setDischargeMode(isFlag());
		return new GuiElectricManager(player.inventory, module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		ModuleElectricManager module = this.getLogisticsModule(player.getEntityWorld(), ModuleElectricManager.class);
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
		return new ElectricModuleSlot(getId());
	}
}
