/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.krapht.gui;

import net.minecraft.src.GuiScreen;
import net.minecraft.src.buildcraft.logisticspipes.modules.IGuiIDHandlerProvider;

public abstract class KraphtBaseGuiScreen extends GuiScreen implements IGuiIDHandlerProvider {
	
	public enum Colors
	{
		White,
		Black,
		LightGrey,
		MiddleGrey,
		DarkGrey,
		Red
	}
	
	protected final int xSize;
	protected final int ySize;
	protected int left;
	protected int right;
	protected int top;
	protected int bottom;
	protected int xCenter;
	protected int yCenter;
	protected final int xCenterOffset;
	protected final int yCenterOffset;
	
	public KraphtBaseGuiScreen(int xSize, int ySize, int xCenterOffset, int yCenterOffset){
		this.xSize = xSize;
		this.ySize = ySize;
		this.xCenterOffset = xCenterOffset;
		this.yCenterOffset = yCenterOffset;
		
	}
	
	@Override
	public void initGui() {
		super.initGui();
		this.left =  width/2 - xSize/2 + xCenterOffset;
		this.top = height/2 - ySize/2  + yCenterOffset;
		
		this.right = width/2 + xSize/2 + xCenterOffset;
		this.bottom = height/2 + ySize/2 + yCenterOffset;
		
		this.xCenter = (right + left) / 2;
		this.yCenter = (bottom + top) / 2;
	}
	
	private int ConvertEnumToColor(Colors color){
		switch(color){
			case Black:
				return 0xFF000000;
			case White:
				return 0xFFFFFFFF;
			case DarkGrey:
				return 0xFF555555;
			case MiddleGrey:
				return 0xFF8b8b8b;
			case LightGrey:
				return 0xFFC6C6C6;
			case Red:
				return 0xFFFF0000;
			
			default: 
				return 0;
			}
	}
	
	public void drawPoint(int x, int y, int color){
		drawRect(x, y, x+1, y+1, color);
	}
	
	public void drawPoint(int x, int y, Colors color){
		drawRect(x, y, x+1, y+1, ConvertEnumToColor(color));
	}
	
	public void drawRect(int x1, int y1, int x2, int y2, Colors color){
		drawRect(x1, y1, x2, y2, ConvertEnumToColor(color));
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
	
	public void drawGuiBackGround(){
		drawRect(left + 2, top, right - 3, top+1, Colors.Black);			// Top border
		drawRect(left + 3, bottom-1, right - 2, bottom, Colors.Black);		// Bottom border
		drawRect(left, top + 2, left+1, bottom -3, Colors.Black);			// Left border
		drawRect(right-1, top + 3, right, bottom - 2, Colors.Black);		// Right border
		
		drawRect(left+3, top + 3, right - 1, bottom -1, Colors.DarkGrey);	//Right/Bottom highlight
		drawRect(left+1, top + 1, right - 3, bottom -3, Colors.White);		//Top/Left highlight
		drawRect(left+3, top + 3, right - 3, bottom - 3, Colors.LightGrey);	// Main background
		
		drawPoint(left + 1, top +1, Colors.Black);							//Top-left border corner
		drawPoint(left+3, top + 3, Colors.White);							//Top-left highlight corner
		drawPoint(right - 3, top + 1, Colors.Black);						//Top-right border corner
		drawPoint(right - 2, top + 2, Colors.Black);						//Top-right border corner
		drawPoint(right - 3, top + 2, Colors.LightGrey);					//Top-right highlight corner
		
		drawPoint(left + 1, bottom -3, Colors.Black);						//Bottom-left border corner 1
		drawPoint(left + 2, bottom -2, Colors.Black);						//Bottom-left border corner 2
		drawPoint(left + 2, bottom -3, Colors.LightGrey);					//Bottom-left highlight corner
		drawPoint(right - 2, bottom -2, Colors.Black);						//Bottom-right border corner
		drawPoint(right - 4, bottom - 4, Colors.DarkGrey);					//Bottom-right highlight corner
	}
}
