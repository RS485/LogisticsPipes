package logisticspipes.interfaces;

public interface IHUDConfig {

	boolean isHUDChassie();

	boolean isHUDCrafting();

	boolean isHUDInvSysCon();

	boolean isHUDPowerLevel();

	boolean isHUDProvider();

	boolean isHUDSatellite();

	void setHUDChassie(boolean state);

	void setHUDCrafting(boolean state);

	void setHUDInvSysCon(boolean state);

	void setHUDPowerJunction(boolean state);

	void setHUDProvider(boolean state);

	void setHUDSatellite(boolean state);
}
