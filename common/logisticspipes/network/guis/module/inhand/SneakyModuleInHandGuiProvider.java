package logisticspipes.network.guis.module.inhand;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.gui.modules.GuiSneakyConfigurator;
import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.DummyModuleContainer;
import network.rs485.logisticspipes.module.SneakyDirection;

@StaticResolve
public class SneakyModuleInHandGuiProvider extends ModuleInHandGuiProvider {

	public SneakyModuleInHandGuiProvider(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsModule module = getLogisticsModule(player);
		if (!(module instanceof LogisticsGuiModule && module instanceof SneakyDirection)) {
			return null;
		}
		return new GuiSneakyConfigurator(player.inventory, (LogisticsGuiModule) module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		DummyModuleContainer dummy = new DummyModuleContainer(player, getInvSlot());
		if (!(dummy.getModule() instanceof LogisticsGuiModule && dummy.getModule() instanceof SneakyDirection)) {
			return null;
		}
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new SneakyModuleInHandGuiProvider(getId());
	}
}
