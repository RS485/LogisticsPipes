package logisticspipes.interfaces.routing;

public interface ILogisticsPowerProvider {
	public boolean useEnergy(int amount);
	public boolean canUseEnergy(int amount);
	public int getPowerLevel();
}
