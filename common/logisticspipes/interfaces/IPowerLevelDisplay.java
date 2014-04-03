package logisticspipes.interfaces;

public interface IPowerLevelDisplay {
	int getChargeState();
	int getDisplayPowerLevel();
	int getMaxStorage();
	boolean isInvalid();
	String getBrand();
}
