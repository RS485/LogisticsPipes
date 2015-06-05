package logisticspipes.gui.hud.modules;

import java.util.List;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.modules.abstractmodules.LogisticsSimpleFilterModule;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.item.ItemStackRenderer;
import logisticspipes.utils.item.ItemStackRenderer.DisplayAmount;

import org.lwjgl.opengl.GL11;

public class HUDSimpleFilterModule implements IHUDModuleRenderer {

	private final LogisticsSimpleFilterModule module;

	public HUDSimpleFilterModule(LogisticsSimpleFilterModule module) {
		this.module = module;
	}

	@Override
	public void renderContent(boolean shifted) {
		GL11.glScalef(1.0F, 1.0F, -0.00001F);
		ItemStackRenderer.renderItemIdentifierStackListIntoGui(ItemIdentifierStack.getListFromInventory(module.getFilterInventory()), null, 0, -25, -32, 3, 9, 18, 18, 100.0F, DisplayAmount.NEVER, true, false, shifted);
		GL11.glScalef(1.0F, 1.0F, 1 / -0.00001F);
	}

	@Override
	public List<IHUDButton> getButtons() {
		return null;
	}
}
