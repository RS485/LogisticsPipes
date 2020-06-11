/*
package logisticspipes.gui.modules;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.modules.ModuleThaumicAspectSink;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.SimpleGraphics;
import logisticspipes.utils.item.ItemIdentifierInventory;

import net.minecraft.inventory.IInventory;

import org.lwjgl.opengl.GL11;

public class GuiThaumicAspectSink extends ModuleBaseGui {

	private final ModuleThaumicAspectSink _module;
	private final ItemIdentifierInventory tmpInv;

	private List<String> stackTags = null;

	private int mouseX = 0;
	private int mouseY = 0;

	public GuiThaumicAspectSink(IInventory playerInventory, ModuleThaumicAspectSink itemSink) {
		super(null, itemSink);

		_module = itemSink;

		tmpInv = new ItemIdentifierInventory(1, "Aspect Reader", 1);

		DummyContainer dummy = new DummyContainer(playerInventory, tmpInv);
		dummy.addDummySlot(0, 7, 7);

		dummy.addNormalSlotsForPlayerInventory(7, 90);

		inventorySlots = dummy;
		xSize = 175;
		ySize = 172;
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
	}

	@Override
	protected void mouseClicked(int i, int j, int k) throws IOException {
		int x = i - guiLeft;
		int y = j - guiTop;
		if (0 < x && x < 175 && 0 < y && y < 172) {
			mouseX = x;
			mouseY = y;
		}
		super.mouseClicked(i, j, k);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);

		mc.fontRenderer.drawString("Aspect Reader", 26, 11, 0x404040);
		mc.fontRenderer.drawString("Sunk Aspects", 8, 48, 0x404040);
		mc.fontRenderer.drawString("Inventory", 8, ySize - 92, 0x404040);

		//handle clicks
		if (6 <= mouseX && mouseX < 6 + 9 * 18 && 24 <= mouseY && mouseY < 24 + 18) {
			int i = (mouseX - 6) / 18;
			if (stackTags != null && i < stackTags.size()) {
				tmpInv.clearInventorySlotContents(0);
				_module.guiAddAspect(stackTags.get(i));
				stackTags.remove(i);
			}
		}
		if (6 <= mouseX && mouseX < 6 + 9 * 18 && 57 <= mouseY && mouseY < 57 + 18) {
			int i = (mouseX - 6) / 18;
			if (_module.aspectList != null && i < _module.aspectList.size()) {
				tmpInv.clearInventorySlotContents(0);
				if (stackTags == null) {
					stackTags = new LinkedList<>();
				}
				if (!stackTags.contains(_module.aspectList.get(i)) && stackTags.size() < 9) {
					stackTags.add(_module.aspectList.get(i));
				}
				_module.guiRemoveAspect(_module.aspectList.get(i));
			}
		}
		mouseX = 0;
		mouseY = 0;

		//transfer tags from stack to gui list
		if (tmpInv.getStackInSlot(0) != null && SimpleServiceLocator.thaumCraftProxy.isScannedObject(tmpInv.getStackInSlot(0), mc.thePlayer.getName())) {
			stackTags = SimpleServiceLocator.thaumCraftProxy.getListOfTagsForStack(tmpInv.getStackInSlot(0));
		}

		//render aspects
		GL11.glEnable(GL11.GL_LIGHTING);
		if (stackTags != null) {
			SimpleServiceLocator.thaumCraftProxy.renderAspectsInGrid(stackTags, 7, 25, 9, 1, this);
		}
		SimpleServiceLocator.thaumCraftProxy.renderAspectsInGrid(_module.aspectList, 7, 58, 9, 1, this);
		GL11.glDisable(GL11.GL_LIGHTING);

		//render mouse-over overlay
		int pointerX = par1 - guiLeft;
		int pointerY = par2 - guiTop;

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		if (6 <= pointerX && pointerX < 6 + 9 * 18 && 24 <= pointerY && pointerY < 24 + 18) {
			int i = (pointerX - 6) / 18;
			if (stackTags != null && i < stackTags.size()) {
				SimpleGraphics.drawRectNoBlend(7 + i * 18, 25, 7 + i * 18 + 16, 25 + 16, Color.WHITE_50, 0.0);
			}
		}
		if (6 <= pointerX && pointerX < 6 + 9 * 18 && 57 <= pointerY && pointerY < 57 + 18) {
			int i = (pointerX - 6) / 18;
			if (_module.aspectList != null && i < _module.aspectList.size()) {
				SimpleGraphics.drawRectNoBlend(7 + i * 18, 58, 7 + i * 18 + 16, 58 + 16, Color.WHITE_50, 0.0);
			}
		}
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, false);
		GuiGraphics.drawPlayerInventoryBackground(mc, guiLeft + 7, guiTop + 90);

		GuiGraphics.drawSlotBackground(mc, guiLeft + 6, guiTop + 6);

		//I can has purple aspect slots
		for (int i = 0; i < 9; i++) {
			GuiGraphics.drawSlotBackground(mc, guiLeft + 6 + i * 18, guiTop + 24, 0xffffe4ff);
		}

		for (int i = 0; i < 9; i++) {
			GuiGraphics.drawSlotBackground(mc, guiLeft + 6 + i * 18, guiTop + 57, 0xffffe4ff);
		}
	}
}
*/