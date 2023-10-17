package logisticspipes.network.guis.module.inpipe;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.gui.modules.GuiSimpleFilter;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;
import network.rs485.logisticspipes.module.SimpleFilter;

@StaticResolve
public class SimpleFilterInventorySlot extends ModuleCoordinatesGuiProvider {

	public SimpleFilterInventorySlot(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsModule module = this.getLogisticsModule(player.getEntityWorld(), LogisticsModule.class);
		if (module == null) {
			return null;
		}
		return new GuiSimpleFilter(player.inventory, module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		return getContainerFromFilterModule(this, player);
	}

	@Nullable
	public static DummyContainer getContainerFromFilterModule(ModuleCoordinatesGuiProvider guiProvider, EntityPlayer player) {
		SimpleFilter filter = guiProvider.getLogisticsModule(player.getEntityWorld(), SimpleFilter.class);
		if (filter == null) {
			return null;
		}
		DummyContainer dummy = new DummyContainer(player.inventory, filter.getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(8, 60);

		//Pipe slots
		for (int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
			dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
		}

		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new SimpleFilterInventorySlot(getId());
	}
}
