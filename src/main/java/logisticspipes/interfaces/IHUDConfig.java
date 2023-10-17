package logisticspipes.interfaces;

public interface IHUDConfig {

	boolean isChassisHUD();

	boolean isHUDCrafting();

	boolean isHUDInvSysCon();

	boolean isHUDPowerLevel();

	boolean isHUDProvider();

	boolean isHUDSatellite();

	void setChassisHUD(boolean state);

	void setHUDCrafting(boolean state);

	void setHUDInvSysCon(boolean state);

	void setHUDPowerJunction(boolean state);

	void setHUDProvider(boolean state);

	void setHUDSatellite(boolean state);
}
