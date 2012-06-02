/** 
 * Copyright (c) Krapht, 2012
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
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.krapht.gui.DummyContainer;

import org.lwjgl.opengl.GL11;

public class GuiExtractor extends GuiContainer{

	//private final SneakyPipe _pipe;
	
	private final ModuleExtractor _extractor;
	private final GuiScreen _previousGui;
	
	public GuiExtractor(IInventory playerInventory, ModuleExtractor extractor, GuiScreen previousGui) {
		super(new DummyContainer(playerInventory, null));
		this._extractor = extractor;
		this._previousGui = previousGui;
		xSize = 160;
		ySize = 200;
	}
	
	@Override
	public void initGui() {
		super.initGui();

		int left = width / 2 - xSize / 2;
		int top = height / 2 - ySize / 2;
		  
		controlList.add(new GuiButton(0, left + 73, top + 23, 20, 20, ""));	//YNEG
		controlList.add(new GuiButton(1, left + 73, top + 43, 20, 20, ""));	//YPOS
		controlList.add(new GuiButton(2, left + 73, top + 63, 20, 20, "")); 	//ZNEG
		controlList.add(new GuiButton(3, left+10, top + 43, 20, 20, ""));	//DEFAULT

		refreshButtons();
	}
	
	private void refreshButtons(){
		for (Object p : controlList){
			GuiButton button = (GuiButton) p;
			switch (button.id){
				case 0: button.displayString =  isExtract(SneakyOrientation.Top); break;
				case 1: button.displayString =  isExtract(SneakyOrientation.Side); break;
				case 2: button.displayString =  isExtract(SneakyOrientation.Bottom); break;
				case 3: button.displayString =  isExtract(SneakyOrientation.Default); break;
			}
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch (guibutton.id){
		case 0:
			_extractor.setSneakyOrientation(SneakyOrientation.Top);
			break;
		
		case 1:
			_extractor.setSneakyOrientation(SneakyOrientation.Side);
			break;
		
		case 2:
			_extractor.setSneakyOrientation(SneakyOrientation.Bottom);
			break;
			
		case 3:
			_extractor.setSneakyOrientation(SneakyOrientation.Default);
			break;
		}
		
		refreshButtons();
		super.actionPerformed(guibutton);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer() {
		super.drawGuiContainerForegroundLayer();
		
		int left = width / 2 - xSize / 2;
		int top = height / 2 - ySize / 2;
		
		fontRenderer.drawString("Extract orientation", xSize / 2 - fontRenderer.getStringWidth("Extract orientation") / 2 , 10, 0x404040);
		fontRenderer.drawString("Default", 35, 50, 0x404040);
		fontRenderer.drawString("Top", 100, 30, 0x404040);
		fontRenderer.drawString("Side", 100, 50, 0x404040);
		fontRenderer.drawString("Bottom", 100, 70, 0x404040);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine.getTexture("/net/minecraft/src/buildcraft/logisticspipes/modules/gui/GuiExtractor.png");
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		//drawRect(width/2 - xSize / 2, height / 2 - ySize /2, width/2 + xSize / 2, height / 2 + ySize /2, 0xFF404040);
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

	private String getButtonText(boolean checked){
		return checked ? "[X]" : "[ ]";
	}

	private String isExtract(SneakyOrientation o){
		return getButtonText(o == _extractor.getSneakyOrientation());
	}
}
