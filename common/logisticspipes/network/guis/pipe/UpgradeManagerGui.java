package logisticspipes.network.guis.pipe;

import logisticspipes.gui.GuiUpgradeManager;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.gui.DummyContainer;

import net.minecraft.entity.player.EntityPlayer;

public class UpgradeManagerGui extends CoordinatesGuiProvider {

	public UpgradeManagerGui(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = getPipe(player.getEntityWorld());
		if (pipe == null || !((pipe.pipe instanceof CoreRoutedPipe))) {
			return null;
		}
		return new GuiUpgradeManager(player, (CoreRoutedPipe) pipe.pipe);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = getPipe(player.getEntityWorld());
		if (pipe == null || !((pipe.pipe instanceof CoreRoutedPipe))) {
			return null;
		}
		return ((CoreRoutedPipe) pipe.pipe).getOriginalUpgradeManager().getDummyContainer(player);
	}

	@Override
	public GuiProvider template() {
		return new UpgradeManagerGui(getId());
	}
}
