package logisticspipes.gui.hud.modules;

import java.util.List;

import org.lwjgl.opengl.GL11;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.item.ItemStackRenderer;
import logisticspipes.utils.item.ItemStackRenderer.DisplayAmount;
import network.rs485.logisticspipes.module.SimpleFilter;

public class HUDSimpleFilterModule implements IHUDModuleRenderer {

	private final SimpleFilter filter;

	public HUDSimpleFilterModule(SimpleFilter filter) {
		this.filter = filter;
	}

	@Override
	public void renderContent(boolean shifted) {
		GL11.glScalef(1.0F, 1.0F, -0.00001F);
		ItemStackRenderer.renderItemIdentifierStackListIntoGui(ItemIdentifierStack.getListFromInventory(filter.getFilterInventory()), null,
				0, -25, -32, 3, 9, 18, 18, 100.0F, DisplayAmount.NEVER, false, shifted);
		GL11.glScalef(1.0F, 1.0F, 1 / -0.00001F);
	}

	@Override
	public List<IHUDButton> getButtons() {
		return null;
	}
}
