package logisticspipes.gui.modules;

import logisticspipes.modules.ModuleCCBasedQuickSort;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.modules.CCBasedQuickSortMode;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.SmallGuiButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.IInventory;

import org.lwjgl.input.Keyboard;

public class GuiCCBasedQuickSort extends GuiWithPreviousGuiContainer {
	
	private ModuleCCBasedQuickSort _sortModule;
	private int								slot;
	
	public GuiCCBasedQuickSort(IInventory playerInventory, CoreRoutedPipe pipe, ModuleCCBasedQuickSort sortModule, GuiScreen previousGui, int slot) {
		super(new DummyContainer(playerInventory, null), pipe, previousGui);
		_sortModule = sortModule;
		xSize = 120;
		ySize = 60;
		this.slot = slot;
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
		switch(guibutton.id) {
			case 0:
				if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
					change = 100;
				} else {
					change = 10;
				}
				break;
			case 1:
				if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
					change = 5;
				} else {
					change = 1;
				}
				break;
			case 2:
				if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
					change = -100;
				} else {
					change = -10;
				}
				break;
			case 3:
				if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
					change = -5;
				} else {
					change = -1;
				}
				break;
		}
		this._sortModule.setTimeout(Math.max(Math.min(this._sortModule.getTimeout() + change, 1000), 5));
		if(slot >= 0) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CCBasedQuickSortMode.class).setInteger2(slot).setInteger(_sortModule.getTimeout()).setPosX(pipe.getX()).setPosY(pipe.getY()).setPosZ(pipe.getZ()));
		} else {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CCBasedQuickSortMode.class).setInteger2(slot).setInteger(_sortModule.getTimeout()).setPosX(0).setPosY(-1).setPosZ(0));
		}
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
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
	}
	
	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Module_CC_Based_QuickSort_ID;
	}

	public void setTimeOut(int timeout) {
		this._sortModule.setTimeout(timeout);
	}
}
