package logisticspipes.pipes.upgrades;

import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;

public interface IPipeUpgrade {

	boolean needsUpdate();

	boolean isAllowedForPipe(CoreRoutedPipe pipe);

	boolean isAllowedForModule(LogisticsModule pipe);

	String[] getAllowedPipes();

	String[] getAllowedModules();
}
