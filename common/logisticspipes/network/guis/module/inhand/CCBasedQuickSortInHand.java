package logisticspipes.network.guis.module.inhand;

import logisticspipes.gui.modules.GuiCCBasedQuickSort;
import logisticspipes.modules.ModuleCCBasedQuickSort;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.DummyModuleContainer;

import net.minecraft.entity.player.EntityPlayer;

public class CCBasedQuickSortInHand extends ModuleInHandGuiProvider {

	public CCBasedQuickSortInHand(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsModule module = getLogisticsModule(player);
		if (!(module instanceof ModuleCCBasedQuickSort)) {
			return null;
		}
		return new GuiCCBasedQuickSort(player.inventory, (ModuleCCBasedQuickSort) module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		DummyModuleContainer dummy = new DummyModuleContainer(player, getInvSlot());
		if (!(dummy.getModule() instanceof ModuleCCBasedQuickSort)) {
			return null;
		}
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new CCBasedQuickSortInHand(getId());
	}
}
