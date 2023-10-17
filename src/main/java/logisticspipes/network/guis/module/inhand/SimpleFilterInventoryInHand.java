package logisticspipes.network.guis.module.inhand;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.gui.modules.GuiSimpleFilter;
import logisticspipes.items.ItemModule;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.DummyModuleContainer;
import network.rs485.logisticspipes.module.Gui;
import network.rs485.logisticspipes.module.SimpleFilter;

@StaticResolve
public class SimpleFilterInventoryInHand extends ModuleInHandGuiProvider {

	public SimpleFilterInventoryInHand(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsModule module = ItemModule.getLogisticsModule(player, getInvSlot());
		if (!(module instanceof Gui && module instanceof SimpleFilter)) {
			return null;
		}
		return new GuiSimpleFilter(player.inventory, module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		DummyModuleContainer dummy = new DummyModuleContainer(player, getInvSlot());
		if (!(dummy.getModule() instanceof SimpleFilter)) {
			return null;
		}
		dummy.setInventory(((SimpleFilter) dummy.getModule()).getFilterInventory());
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
