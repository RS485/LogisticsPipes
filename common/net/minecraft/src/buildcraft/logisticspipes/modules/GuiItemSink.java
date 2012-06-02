/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.IInventory;
import net.minecraft.src.ModLoader;
import net.minecraft.src.krapht.gui.DummyContainer;

import org.lwjgl.opengl.GL11;

public class GuiItemSink extends GuiContainer {

	private final IInventory _playerInventory;
	private final ModuleItemSink _itemSink;
	private final GuiScreen _previousGui;
	
	
	@Override
	public void initGui() {
		super.initGui();
       //Default item toggle:
       controlList.clear();
       controlList.add(new GuiButton(0, width / 2 + 50, height / 2 - 34, 30, 20, _itemSink.isDefaultRoute() ? "Yes" : "No"));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch(guibutton.id)
		{
			case 0:
				_itemSink.setDefaultRoute(!_itemSink.isDefaultRoute());
				((GuiButton)controlList.get(0)).displayString = _itemSink.isDefaultRoute() ? "Yes" : "No";
				break;
		}
		
	}
	
	public GuiItemSink(IInventory playerInventory, ModuleItemSink itemSink, GuiScreen previousGui) {
		super(null);
		_itemSink = itemSink;
		_previousGui = previousGui;
		DummyContainer dummy = new DummyContainer(playerInventory, _itemSink.getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(8, 60);

		//Pipe slots
	    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
	    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
	    }
	    
	    this.inventorySlots = dummy;
		this._playerInventory = playerInventory;
		xSize = 175;
		ySize = 142;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer() {
		fontRenderer.drawString(_itemSink.getFilterInventory().getInvName(), 8, 6, 0x404040);
		fontRenderer.drawString("Inventory", 8, ySize - 92, 0x404040);
		fontRenderer.drawString("Default route:", 65, 45, 0x404040);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine.getTexture("/net/minecraft/src/buildcraft/logisticspipes/modules/gui/GuiItemSink.png");
				
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}
	
	@Override
	protected void keyTyped(char c, int i) {
		if (i == 1 || c == 'e'){
			if (_previousGui != null){
				_previousGui.initGui();
				mc.currentScreen = _previousGui;
			} else {
				super.keyTyped(c, i);
			}
		}
	}

	//int inventoryRows = 1;
}
