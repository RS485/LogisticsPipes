package logisticspipes.modules.abstractmodules;

import net.minecraftforge.common.ForgeDirection;

public abstract class LogisticsSneakyDirectionModule extends LogisticsGuiModule {
	public abstract ForgeDirection getSneakyDirection();
	public abstract void setSneakyDirection(ForgeDirection sneakyDirection);
}
