package logisticspipes.modplugins.jei;

import java.awt.Rectangle;
import java.util.List;

import mezz.jei.api.gui.IAdvancedGuiHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import logisticspipes.utils.gui.LogisticsBaseGuiScreen;

public class AdvancedGuiHandler implements IAdvancedGuiHandler<LogisticsBaseGuiScreen> {

	@Override
	public @NotNull Class<LogisticsBaseGuiScreen> getGuiContainerClass() {
		return LogisticsBaseGuiScreen.class;
	}

	@Nullable
	@Override
	public List<Rectangle> getGuiExtraAreas(@NotNull LogisticsBaseGuiScreen guiContainer) {
		return guiContainer.getGuiExtraAreas();
	}
}
