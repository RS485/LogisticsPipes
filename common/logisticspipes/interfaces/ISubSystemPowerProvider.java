package logisticspipes.interfaces;

public interface ISubSystemPowerProvider {

	double getPowerLevel();

	void requestPower(int destination, double amount);

	boolean usePaused();

	String getBrand();
}
