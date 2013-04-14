/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui.modules;

import logisticspipes.modules.ModuleItemSink;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiStringHandlerButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.IInventory;

import org.lwjgl.opengl.GL11;

import buildcraft.transport.Pipe;

public class GuiItemSink extends GuiWithPreviousGuiContainer {

	private final ModuleItemSink _itemSink;
	private final int slot;
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
       //Default item toggle:
       buttonList.clear();
       buttonList.add(new GuiStringHandlerButton(0, width / 2 + 50, height / 2 - 34, 30, 20, new GuiStringHandlerButton.StringHandler(){
		@Override
		public String getContent() {
			return _itemSink.isDefaultRoute() ? "Yes" : "No";
		}}));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch(guibutton.id)
		{
			case 0:
				_itemSink.setDefaultRoute(!_itemSink.isDefaultRoute());
				//((GuiButton)buttonList.get(0)).displayString = _itemSink.isDefaultRoute() ? "Yes" : "No";
				if(slot != 20) {
					MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.ITEM_SINK_DEFAULT, pipe.xCoord, pipe.yCoord, pipe.zCoord, (_itemSink.isDefaultRoute() ? 1 : 0) + (slot * 10)).getPacket());
				} else {
					MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.ITEM_SINK_DEFAULT, 0, -1, 0, (_itemSink.isDefaultRoute() ? 1 : 0) + (slot * 10)).getPacket());
				}
				break;
		}
		
	}
	
	public GuiItemSink(IInventory playerInventory, Pipe pipe, ModuleItemSink itemSink, GuiScreen previousGui, int slot) {
		super(null,pipe,previousGui);
		_itemSink = itemSink;
		this.slot = slot;
		DummyContainer dummy = new DummyContainer(playerInventory, _itemSink.getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(8, 60);

		//Pipe slots
	    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
	    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
	    }
	    
	    this.inventorySlots = dummy;
		xSize = 175;
		ySize = 142;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRenderer.drawString(_itemSink.getFilterInventory().getInvName(), 8, 6, 0x404040);
		fontRenderer.drawString("Inventory", 8, ySize - 92, 0x404040);
		fontRenderer.drawString("Default route:", 65, 45, 0x404040);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture("/logisticspipes/gui/itemsink.png");
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Module_ItemSink_ID;
	}
}
