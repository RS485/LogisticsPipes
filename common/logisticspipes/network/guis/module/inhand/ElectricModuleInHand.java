package logisticspipes.network.guis.module.inhand;

import logisticspipes.gui.modules.GuiElectricManager;
import logisticspipes.modules.ModuleElectricManager;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.DummyModuleContainer;

import net.minecraft.entity.player.EntityPlayer;

public class ElectricModuleInHand extends ModuleInHandGuiProvider {

	public ElectricModuleInHand(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsModule module = getLogisticsModule(player);
		if (!(module instanceof ModuleElectricManager)) {
			return null;
		}
		return new GuiElectricManager(player.inventory, (ModuleElectricManager) module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		DummyModuleContainer dummy = new DummyModuleContainer(player, getInvSlot());
		if (!(dummy.getModule() instanceof ModuleElectricManager)) {
			return null;
		}
		dummy.setInventory(((ModuleElectricManager) dummy.getModule()).getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(8, 60);
		//Pipe slots
		for (int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
			dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
		}
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new ElectricModuleInHand(getId());
	}
}
