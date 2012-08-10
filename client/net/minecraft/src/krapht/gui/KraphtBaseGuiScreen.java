/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.krapht.gui;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.Tessellator;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.logisticspipes.modules.IGuiIDHandlerProvider;

public abstract class KraphtBaseGuiScreen extends GuiContainer implements IGuiIDHandlerProvider, ISubGuiControler {
	
	public enum Colors
	{
		White,
		Black,
		LightGrey,
		MiddleGrey,
		DarkGrey,
		Red
	}
	
	protected int right;
	protected int bottom;
	protected int xCenter;
	protected int yCenter;
	protected final int xCenterOffset;
	protected final int yCenterOffset;
	
	private SubGuiScreen subGui;
	
	public KraphtBaseGuiScreen(int xSize, int ySize, int xCenterOffset, int yCenterOffset){
		super(new DummyContainer(null, null));
		this.xSize = xSize;
		this.ySize = ySize;
		this.xCenterOffset = xCenterOffset;
		this.yCenterOffset = yCenterOffset;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		this.guiLeft =  width/2 - xSize/2 + xCenterOffset;
		this.guiTop = height/2 - ySize/2  + yCenterOffset;
		
		this.right = width/2 + xSize/2 + xCenterOffset;
		this.bottom = height/2 + ySize/2 + yCenterOffset;
		
		this.xCenter = (right + guiLeft) / 2;
		this.yCenter = (bottom + guiTop) / 2;
	}
	
	public boolean hasSubGui() {
		return subGui != null;
	}
	
	public SubGuiScreen getSubGui() {
		return subGui;
	}
	
	public void setSubGui(SubGuiScreen gui) {
		if(subGui == null) {
			subGui = gui;
			subGui.setWorldAndResolution(this.mc, this.width, this.height);
			subGui.register(this);
			subGui.initGui();
		}
	}

	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
		if(subGui != null)
			subGui.setWorldAndResolution(mc, width, height);
	}
	
	public void resetSubGui() {
		subGui = null;
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3){
		super.drawScreen(par1, par2, par3);
		if(subGui != null) {
			if(!subGui.hasSubGui()) {
				super.drawDefaultBackground();
			}
			subGui.drawScreen(par1, par2, par3);
		}
	}
	
	@Override
    public final void handleMouseInput() {
		if(subGui != null) {
			subGui.handleMouseInput();
		} else {
			this.handleMouseInputSub();
		}
    }
	
	public void handleMouseInputSub() {
		super.handleMouseInput();
	}
	
	@Override
	public final void handleKeyboardInput() {
		if(subGui != null) {
			subGui.handleKeyboardInput();
		} else {
			this.handleKeyboardInputSub();
		}
	}

	public void handleKeyboardInputSub() {
		super.handleKeyboardInput();
	}
	
	public void drawPoint(int x, int y, int color){
		drawRect(x, y, x+1, y+1, color);
	}
	
	public void drawPoint(int x, int y, Colors color){
		drawRect(x, y, x+1, y+1, BasicGuiHelper.ConvertEnumToColor(color));
	}
	
	public void drawRect(int x1, int y1, int x2, int y2, Colors color){
		drawRect(x1, y1, x2, y2, BasicGuiHelper.ConvertEnumToColor(color));
	}
	
	public void drawLine(int x1, int y1, int x2, int y2, Colors color){
		int lasty = y1;
		for (int dx = 0; x1 + dx < x2; dx++){
			int plotx = x1 + dx;
			int ploty = y1 + (int)((y2 - y1) / (x2-x1-1)) * dx;
			drawPoint(plotx, ploty, color);
			while(lasty < ploty){
				drawPoint(plotx,++lasty, color);
			}
			while (lasty > ploty){
				drawPoint(plotx, --lasty, color);
			}
		}
		while(lasty < y2){
			drawPoint(x2,++lasty, color);
		}
		while (lasty > y2){
			drawPoint(x2, --lasty, color);
		}
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		
	}
	/*
	@Override
	protected void keyTyped(char c, int i) {
		if(i == 1){
			this.mc.displayGuiScreen((GuiScreen)null);
			this.mc.setIngameFocus();
		} else {
			super.keyTyped(c, i);
		}
	}*/
}
