package logisticspipes.gui.hud;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

import logisticspipes.interfaces.IHUDConfig;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.item.ItemStackRenderer;
import logisticspipes.utils.item.ItemStackRenderer.DisplayAmount;

public class HUDCrafting extends BasicHUDGui {

	private final PipeItemsCraftingLogistics pipe;

	public HUDCrafting(PipeItemsCraftingLogistics pipe) {
		this.pipe = pipe;
	}

	@Override
	public void renderHeadUpDisplay(double d, boolean day, boolean shifted, Minecraft mc, IHUDConfig config) {
		if (day) {
			GL11.glColor4b((byte) 64, (byte) 64, (byte) 64, (byte) 64);
		} else {
			GL11.glColor4b((byte) 127, (byte) 127, (byte) 127, (byte) 64);
		}
		if (pipe.displayList.size() > 0) {
			GuiGraphics.drawGuiBackGround(mc, -50, -28, 50, 30, 0, false);
		} else {
			GuiGraphics.drawGuiBackGround(mc, -30, -22, 30, 25, 0, false);
		}
		if (day) {
			GL11.glColor4b((byte) 64, (byte) 64, (byte) 64, (byte) 127);
		} else {
			GL11.glColor4b((byte) 127, (byte) 127, (byte) 127, (byte) 127);
		}

		GL11.glTranslatef(0.0F, 0.0F, -0.005F);
		GL11.glScalef(1.5F, 1.5F, 0.0001F);

		if (pipe.displayList.size() > 0) {
			String message = "Result:";
			mc.fontRenderer.drawString(message, -28, -10, 0);
			message = "Todo:";
			mc.fontRenderer.drawString(message, -28, 5, 0);
		} else {
			String message = "Result:";
			mc.fontRenderer.drawString(message, -16, -10, 0);
		}
		GL11.glScalef(0.8F, 0.8F, -1F);
		List<ItemIdentifierStack> list = new ArrayList<>();
		List<ItemIdentifierStack> craftables = pipe.getCraftedItems();
		if (craftables != null && craftables.size() > 0) {
			//TODO: handle multiple crafables.
			list.add(craftables.get(0));
		}
		if (pipe.displayList.size() > 0) {
			ItemStackRenderer.renderItemIdentifierStackListIntoGui(list, null, 0, 11, -18, 1, 1, 18, 18, 100.0F, DisplayAmount.ALWAYS, false, shifted);
			ItemStackRenderer.renderItemIdentifierStackListIntoGui(pipe.displayList, null, 0, 13, 3, 1, 1, 18, 18, 100.0F, DisplayAmount.ALWAYS, false, shifted);
		} else {
			ItemStackRenderer.renderItemIdentifierStackListIntoGui(list, null, 0, -9, -1, 1, 1, 18, 18, 100.0F, DisplayAmount.ALWAYS, false, shifted);
		}
	}

	@Override
	public boolean display(IHUDConfig config) {
		return config.isHUDCrafting() && ((!pipe.hasCraftingSign() && pipe.getCraftedItems() != null) || pipe.displayList.size() > 0);
	}

	@Override
	public boolean cursorOnWindow(int x, int y) {
		if (pipe.displayList.size() > 0) {
			return -50 < x && x < 50 && -28 < y && y < 30;
		} else {
			return -30 < x && x < 30 && -22 < y && y < 25;
		}
	}
}
