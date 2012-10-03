package logisticspipes.interfaces.routing;

public interface IPowerRouter {
	public ILogisticsPowerProvider getPowerProvider();
	public ILogisticsPowerProvider getConnectedPowerProvider();
}
