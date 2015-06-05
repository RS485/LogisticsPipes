package logisticspipes.network.guis.module.inhand;

import logisticspipes.gui.modules.GuiApiaristAnalyser;
import logisticspipes.modules.ModuleApiaristAnalyser;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.DummyModuleContainer;

import net.minecraft.entity.player.EntityPlayer;

public class ApiaristAnalyserModuleInHand extends ModuleInHandGuiProvider {

	public ApiaristAnalyserModuleInHand(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsModule module = getLogisticsModule(player);
		if (!(module instanceof ModuleApiaristAnalyser)) {
			return null;
		}
		return new GuiApiaristAnalyser((ModuleApiaristAnalyser) module, player.inventory);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		DummyModuleContainer dummy = new DummyModuleContainer(player, getInvSlot());
		if (!(dummy.getModule() instanceof ModuleApiaristAnalyser)) {
			return null;
		}
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new ApiaristAnalyserModuleInHand(getId());
	}
}
