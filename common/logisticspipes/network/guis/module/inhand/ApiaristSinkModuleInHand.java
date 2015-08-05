package logisticspipes.network.guis.module.inhand;

import logisticspipes.gui.modules.GuiApiaristSink;
import logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.DummyModuleContainer;

import net.minecraft.entity.player.EntityPlayer;

public class ApiaristSinkModuleInHand extends ModuleInHandGuiProvider {

	public ApiaristSinkModuleInHand(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsModule module = getLogisticsModule(player);
		if (!(module instanceof ModuleApiaristSink)) {
			return null;
		}
		return new GuiApiaristSink((ModuleApiaristSink) module, player);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		SimpleServiceLocator.forestryProxy.syncTracker(player.getEntityWorld(), player);
		DummyModuleContainer dummy = new DummyModuleContainer(player, getInvSlot());
		if (!(dummy.getModule() instanceof ModuleApiaristSink)) {
			return null;
		}
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new ApiaristSinkModuleInHand(getId());
	}
}
