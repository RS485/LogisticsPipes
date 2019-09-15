package logisticspipes.modules.abstractmodules;

import net.minecraft.util.math.Direction;

public abstract class LogisticsSneakyDirectionModule extends LogisticsGuiModule {

	public abstract Direction getSneakyDirection();

	public abstract void setSneakyDirection(Direction sneakyDirection);
}
