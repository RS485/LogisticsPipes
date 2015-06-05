package logisticspipes.network.guis.module.inhand;

import logisticspipes.gui.modules.GuiSimpleFilter;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.modules.abstractmodules.LogisticsSimpleFilterModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.DummyModuleContainer;

import net.minecraft.entity.player.EntityPlayer;

public class SimpleFilterInventoryInHand extends ModuleInHandGuiProvider {

	public SimpleFilterInventoryInHand(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsModule module = getLogisticsModule(player);
		if (!(module instanceof LogisticsSimpleFilterModule)) {
			return null;
		}
		return new GuiSimpleFilter(player.inventory, (LogisticsSimpleFilterModule) module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		DummyModuleContainer dummy = new DummyModuleContainer(player, getInvSlot());
		if (!(dummy.getModule() instanceof LogisticsSimpleFilterModule)) {
			return null;
		}
		dummy.setInventory(((LogisticsSimpleFilterModule) dummy.getModule()).getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(8, 60);

		//Pipe slots
		for (int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
			dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
		}

		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new SimpleFilterInventoryInHand(getId());
	}
}
