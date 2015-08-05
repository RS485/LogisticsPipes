package logisticspipes.network.guis.module.inpipe;

import logisticspipes.gui.modules.GuiApiaristSink;
import logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.NBTModuleCoordinatesGuiProvider;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.gui.DummyContainer;

import net.minecraft.entity.player.EntityPlayer;

public class ApiaristSinkModuleSlot extends NBTModuleCoordinatesGuiProvider {

	public ApiaristSinkModuleSlot(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		ModuleApiaristSink module = this.getLogisticsModule(player.getEntityWorld(), ModuleApiaristSink.class);
		if (module == null) {
			return null;
		}
		module.readFromNBT(getNbt());
		return new GuiApiaristSink(module, player);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		SimpleServiceLocator.forestryProxy.syncTracker(player.getEntityWorld(), player);
		ModuleApiaristSink module = this.getLogisticsModule(player.getEntityWorld(), ModuleApiaristSink.class);
		if (module == null) {
			return null;
		}
		return new DummyContainer(player.inventory, null);
	}

	@Override
	public GuiProvider template() {
		return new ApiaristSinkModuleSlot(getId());
	}
}
