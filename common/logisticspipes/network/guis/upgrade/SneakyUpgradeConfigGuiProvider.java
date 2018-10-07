package logisticspipes.network.guis.upgrade;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.gui.popup.SneakyConfigurationPopup;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.UpgradeCoordinatesGuiProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.gui.UpgradeSlot;
import network.rs485.logisticspipes.world.DoubleCoordinates;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

import logisticspipes.utils.StaticResolve;

@StaticResolve
public class SneakyUpgradeConfigGuiProvider extends UpgradeCoordinatesGuiProvider {

	public SneakyUpgradeConfigGuiProvider(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsTileGenericPipe bPipe = getPipe(player.getEntityWorld());
		if(bPipe != null && bPipe.pipe instanceof CoreRoutedPipe) {
			List<DoubleCoordinates> list = new WorldCoordinatesWrapper(bPipe).getConnectedAdjacentTileEntities().filter(in -> SimpleServiceLocator.pipeInformationManager.isNotAPipe(in.tileEntity)).map(in -> new DoubleCoordinates(in.tileEntity)).collect(Collectors.toList());
			if(list.isEmpty()) {
				new WorldCoordinatesWrapper(bPipe).getConnectedAdjacentTileEntities().map(in -> new DoubleCoordinates(in.tileEntity)).forEach(list::add);
			}
			return new SneakyConfigurationPopup(list, getSlot(player, UpgradeSlot.class));
		}
		return null;
	}

	@Override
	public GuiProvider template() {
		return new SneakyUpgradeConfigGuiProvider(getId());
	}
}
