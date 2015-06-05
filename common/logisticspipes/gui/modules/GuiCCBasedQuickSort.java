package logisticspipes.gui.modules;

import logisticspipes.modules.ModuleCCBasedQuickSort;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.modules.CCBasedQuickSortMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.SmallGuiButton;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;

import org.lwjgl.input.Keyboard;

public class GuiCCBasedQuickSort extends ModuleBaseGui {

	private ModuleCCBasedQuickSort _sortModule;

	public GuiCCBasedQuickSort(IInventory playerInventory, ModuleCCBasedQuickSort sortModule) {
		super(new DummyContainer(playerInventory, null), sortModule);
		_sortModule = sortModule;
		xSize = 120;
		ySize = 60;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		int left = width / 2 - xSize / 2;
		int top = height / 2 - ySize / 2;

		buttonList.add(new SmallGuiButton(0, left + 18, top + 22, 15, 10, "++"));
		buttonList.add(new SmallGuiButton(1, left + 18, top + 37, 15, 10, "+"));
		buttonList.add(new SmallGuiButton(2, left + 87, top + 22, 15, 10, "--"));
		buttonList.add(new SmallGuiButton(3, left + 87, top + 37, 15, 10, "-"));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		int change = 0;
		switch (guibutton.id) {
			case 0:
				if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
					change = 100;
				} else {
					change = 10;
				}
				break;
			case 1:
				if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
					change = 5;
				} else {
					change = 1;
				}
				break;
			case 2:
				if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
					change = -100;
				} else {
					change = -10;
				}
				break;
			case 3:
				if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
					change = -5;
				} else {
					change = -1;
				}
				break;
		}
		_sortModule.setTimeout(Math.max(Math.min(_sortModule.getTimeout() + change, 1000), 5));
		MainProxy.sendPacketToServer(PacketHandler.getPacket(CCBasedQuickSortMode.class).setTimeOut(_sortModule.getTimeout()).setModulePos(_sortModule));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		mc.fontRenderer.drawString("Timeout Timer", xSize / 2 - mc.fontRenderer.getStringWidth("Timeout Timer") / 2, 10, 0x404040);
		String timeoutString = Integer.toString(_sortModule.getTimeout()) + " ticks";
		mc.fontRenderer.drawString(timeoutString, xSize / 2 - mc.fontRenderer.getStringWidth(timeoutString) / 2, 30, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
	}
}
