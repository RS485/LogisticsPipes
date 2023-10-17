package logisticspipes.modplugins.jei;

import java.awt.Rectangle;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mezz.jei.api.gui.IAdvancedGuiHandler;

import logisticspipes.utils.gui.LogisticsBaseGuiScreen;

public class AdvancedGuiHandler implements IAdvancedGuiHandler<LogisticsBaseGuiScreen> {

	@Override
	public @Nonnull Class<LogisticsBaseGuiScreen> getGuiContainerClass() {
		return LogisticsBaseGuiScreen.class;
	}

	@Nullable
	@Override
	public List<Rectangle> getGuiExtraAreas(@Nonnull LogisticsBaseGuiScreen guiContainer) {
		return guiContainer.getGuiExtraAreas();
	}
}
