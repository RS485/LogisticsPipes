package logisticspipes.interfaces;

import net.minecraftforge.common.ForgeDirection;

public interface ISneakyDirectionReceiver {
	public ForgeDirection getSneakyDirection();
	public void setSneakyDirection(ForgeDirection sneakyDirection);
	public int getZPos();
}
