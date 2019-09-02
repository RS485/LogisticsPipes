package logisticspipes.network.guis.module.inhand;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.gui.modules.GuiExtractor;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.modules.abstractmodules.LogisticsSneakyDirectionModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.DummyModuleContainer;

@StaticResolve
public class ExtractorModuleInHand extends ModuleInHandGuiProvider {

	public ExtractorModuleInHand(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsModule module = getLogisticsModule(player);
		if (!(module instanceof LogisticsSneakyDirectionModule)) {
			return null;
		}
		return new GuiExtractor(player.inventory, (LogisticsSneakyDirectionModule) module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		DummyModuleContainer dummy = new DummyModuleContainer(player, getInvSlot());
		if (!(dummy.getModule() instanceof LogisticsSneakyDirectionModule)) {
			return null;
		}
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new ExtractorModuleInHand(getId());
	}
}
