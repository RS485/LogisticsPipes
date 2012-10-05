package logisticspipes.interfaces.routing;

import java.util.List;

public interface IPowerRouter {
	public List<ILogisticsPowerProvider> getPowerProvider();
	public List<ILogisticsPowerProvider> getConnectedPowerProvider();
}
