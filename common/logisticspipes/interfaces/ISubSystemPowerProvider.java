package logisticspipes.interfaces;

public interface ISubSystemPowerProvider {

	public float getPowerLevel();

	public void requestPower(int destination, float amount);

	public boolean usePaused();

	public String getBrand();
}
