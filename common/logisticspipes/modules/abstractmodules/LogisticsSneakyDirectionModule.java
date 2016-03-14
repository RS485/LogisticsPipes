package logisticspipes.modules.abstractmodules;

import net.minecraft.util.EnumFacing;

public abstract class LogisticsSneakyDirectionModule extends LogisticsGuiModule {

	public abstract EnumFacing getSneakyDirection();

	public abstract void setSneakyDirection(EnumFacing sneakyDirection);
}
