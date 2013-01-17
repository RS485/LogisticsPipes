package logisticspipes.interfaces;

public interface IChassiePowerProvider {
	public boolean useEnergy(int amount);
	public boolean canUseEnergy(int amount);
}
