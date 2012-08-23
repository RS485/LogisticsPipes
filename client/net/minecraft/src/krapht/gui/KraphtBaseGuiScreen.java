/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.krapht.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Container;
import net.minecraft.src.GuiContainer;
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
	
	private List<IRenderSlot> slots = new ArrayList<IRenderSlot>();

	public KraphtBaseGuiScreen(int xSize, int ySize, int xCenterOffset, int yCenterOffset){
		super(new DummyContainer(null, null));
		this.xSize = xSize;
		this.ySize = ySize;
		this.xCenterOffset = xCenterOffset;
		this.yCenterOffset = yCenterOffset;
	}
	
	public KraphtBaseGuiScreen(Container container){
		super(container);
		this.xCenterOffset = 0;
		this.yCenterOffset = 0;
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
		} else {
			for(IRenderSlot slot:slots) {
				int mouseX = par1 - guiLeft;
				int mouseY = par2 - guiTop;
				int mouseXMax = mouseX - slot.getSize();
				int mouseYMax = mouseY - slot.getSize();
				if(slot.getXPos() < mouseX && slot.getXPos() > mouseXMax && slot.getYPos() < mouseY && slot.getYPos() > mouseYMax) {
					if(slot.displayToolTip()) {
						if(slot.getToolTipText() != null && !slot.getToolTipText().equals("")) {
							ArrayList<String> list = new ArrayList<String>();
							list.add(slot.getToolTipText());
							BasicGuiHelper.drawToolTip(par1 + guiLeft, par2 + guiTop, list, 0, false);
						}
					}
				}
			}
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
	
	public void addRenderSlot(IRenderSlot slot) {
		this.slots.add(slot);
	}
	
	protected void drawGuiContainerForegroundLayer() {
		for(IRenderSlot slot:slots) {
			if(slot instanceof IItemTextureRenderSlot) {
				if(slot.drawSlotBackground()) 
					BasicGuiHelper.drawSlotBackground(mc, slot.getXPos(), slot.getYPos());
				if(((IItemTextureRenderSlot)slot).drawSlotIcon() && !((IItemTextureRenderSlot)slot).customRender(mc, zLevel)) 
					BasicGuiHelper.renderIconAt(mc, slot.getXPos() + 1, slot.getYPos() + 1, zLevel, ((IItemTextureRenderSlot)slot).getTextureId(), ((IItemTextureRenderSlot)slot).getTextureFile());
			} else if(slot instanceof ISmallColorRenderSlot) {
				if(slot.drawSlotBackground())
					BasicGuiHelper.drawSmallSlotBackground(mc, slot.getXPos(), slot.getYPos());
				if(((ISmallColorRenderSlot)slot).drawColor()) 
					drawRect(slot.getXPos() + 1, slot.getYPos() + 1, slot.getXPos() + 7, slot.getYPos() + 7, ((ISmallColorRenderSlot)slot).getColor());
			}
		}
	}

	protected void mouseClicked(int par1, int par2, int par3) {
		for(IRenderSlot slot:slots) {
			int mouseX = par1 - guiLeft;
			int mouseY = par2 - guiTop;
			int mouseXMax = mouseX - slot.getSize();
			int mouseYMax = mouseY - slot.getSize();
			if(slot.getXPos() < mouseX && slot.getXPos() > mouseXMax && slot.getYPos() < mouseY && slot.getYPos() > mouseYMax) {
				slot.mouseClicked(par3);
				return;
			}
		}
		super.mouseClicked(par1, par2, par3);
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
}
