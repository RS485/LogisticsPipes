package logisticspipes.network.guis.module.inhand;

import logisticspipes.modules.ModuleProvider;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.DummyModuleContainer;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.utils.StaticResolve;

@StaticResolve
public class ProviderModuleInHand extends ModuleInHandGuiProvider {

	public ProviderModuleInHand(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsModule module = getLogisticsModule(player);
		if (!(module instanceof ModuleProvider)) {
			return null;
		}
		return new logisticspipes.gui.modules.GuiProvider(player.inventory, (ModuleProvider) module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		DummyModuleContainer dummy = new DummyModuleContainer(player, getInvSlot());
		if (!(dummy.getModule() instanceof ModuleProvider)) {
			return null;
		}
		dummy.setInventory(((ModuleProvider) dummy.getModule()).getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(18, 97);

		int xOffset = 72;
		int yOffset = 18;

		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 3; column++) {
				dummy.addDummySlot(column + row * 3, xOffset + column * 18, yOffset + row * 18);
			}
		}
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new ProviderModuleInHand(getId());
	}
}
