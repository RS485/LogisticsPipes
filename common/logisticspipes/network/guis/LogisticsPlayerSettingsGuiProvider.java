package logisticspipes.network.guis;

import logisticspipes.gui.GuiLogisticsSettings;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.utils.gui.DummyContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class LogisticsPlayerSettingsGuiProvider extends GuiProvider {

	public LogisticsPlayerSettingsGuiProvider(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		return new GuiLogisticsSettings(player);
	}

	@Override
	public Container getContainer(EntityPlayer player) {
		DummyContainer dummy = new DummyContainer(player.inventory, null);
		dummy.addNormalSlotsForPlayerInventory(0, 0); // server does not care where the slots are
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new LogisticsPlayerSettingsGuiProvider(getId());
	}
}
