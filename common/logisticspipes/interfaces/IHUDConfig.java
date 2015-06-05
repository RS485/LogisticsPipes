package logisticspipes.interfaces;

public interface IHUDConfig {

	public boolean isHUDChassie();

	public boolean isHUDCrafting();

	public boolean isHUDInvSysCon();

	public boolean isHUDPowerLevel();

	public boolean isHUDProvider();

	public boolean isHUDSatellite();

	public void setHUDChassie(boolean state);

	public void setHUDCrafting(boolean state);

	public void setHUDInvSysCon(boolean state);

	public void setHUDPowerJunction(boolean state);

	public void setHUDProvider(boolean state);

	public void setHUDSatellite(boolean state);
}
