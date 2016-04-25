package logisticspipes.pipes.upgrades;

import logisticspipes.network.abstractguis.UpgradeCoordinatesGuiProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;

public interface IConfigPipeUpgrade extends IPipeUpgrade {
	public UpgradeCoordinatesGuiProvider getGUI();
}
