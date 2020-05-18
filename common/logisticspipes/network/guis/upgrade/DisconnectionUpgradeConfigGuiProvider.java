package logisticspipes.network.guis.upgrade;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.gui.popup.DisconnectionConfigurationPopup;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.UpgradeCoordinatesGuiProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.UpgradeSlot;

@StaticResolve
public class DisconnectionUpgradeConfigGuiProvider extends UpgradeCoordinatesGuiProvider {

	public DisconnectionUpgradeConfigGuiProvider(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsTileGenericPipe bPipe = getTileAs(player.world, LogisticsTileGenericPipe.class);
		if (!(bPipe.pipe instanceof CoreRoutedPipe)) {
			return null;
		}

		return new DisconnectionConfigurationPopup((CoreRoutedPipe) bPipe.pipe, getSlot(player, UpgradeSlot.class));
	}

	@Override
	public GuiProvider template() {
		return new DisconnectionUpgradeConfigGuiProvider(getId());
	}
}
