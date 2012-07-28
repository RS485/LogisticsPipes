/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.krapht.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.Tessellator;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.logisticspipes.modules.IGuiIDHandlerProvider;

public abstract class KraphtBaseGuiScreen extends GuiContainer implements IGuiIDHandlerProvider {
	
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
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		
	}
	
	@Override
	protected void keyTyped(char c, int i) {
		if(i == 1){
			this.mc.displayGuiScreen((GuiScreen)null);
			this.mc.setIngameFocus();
		} else {
			super.keyTyped(c, i);
		}
	}
	
	public void drawGuiBackGround(){

		int i = mc.renderEngine.getTexture("/logisticspipes/gui/GuiBackground.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);

		//Top Side
		Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(guiLeft + 5	, guiTop + 15	, (double)this.zLevel, 0.33	, 0.33);
        var9.addVertexWithUV(right - 5		, guiTop + 15	, (double)this.zLevel, 0.66	, 0.33);
        var9.addVertexWithUV(right - 5		, guiTop		, (double)this.zLevel, 0.66	, 0);
        var9.addVertexWithUV(guiLeft + 5	, guiTop		, (double)this.zLevel, 0.33	, 0);
        var9.draw();

        //Left Side
        var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(guiLeft		, bottom -5		, (double)this.zLevel, 0	, 0.66);
        var9.addVertexWithUV(guiLeft + 15	, bottom - 5	, (double)this.zLevel, 0.33	, 0.66);
        var9.addVertexWithUV(guiLeft + 15	, guiTop + 5	, (double)this.zLevel, 0.33	, 0.33);
        var9.addVertexWithUV(guiLeft		, guiTop + 5	, (double)this.zLevel, 0	, 0.33);
        var9.draw();

        //Bottom Side
        var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(guiLeft + 5	, bottom		, (double)this.zLevel, 0.33	, 1);
        var9.addVertexWithUV(right - 5		, bottom		, (double)this.zLevel, 0.66	, 1);
        var9.addVertexWithUV(right - 5		, bottom - 15	, (double)this.zLevel, 0.66	, 0.66);
        var9.addVertexWithUV(guiLeft + 5	, bottom - 15	, (double)this.zLevel, 0.33	, 0.66);
        var9.draw();

        //Right Side
        var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(right - 15	, bottom - 5		, (double)this.zLevel, 0.66	, 0.66);
        var9.addVertexWithUV(right		, bottom - 5		, (double)this.zLevel, 1	, 0.66);
        var9.addVertexWithUV(right		, guiTop + 5		, (double)this.zLevel, 1	, 0.33);
        var9.addVertexWithUV(right - 15	, guiTop + 5		, (double)this.zLevel, 0.66	, 0.33);
        var9.draw();
		
		//Top Left
		var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(guiLeft		, guiTop + 15	, (double)this.zLevel, 0	, 0.33);
        var9.addVertexWithUV(guiLeft + 15	, guiTop + 15	, (double)this.zLevel, 0.33	, 0.33);
        var9.addVertexWithUV(guiLeft + 15	, guiTop		, (double)this.zLevel, 0.33	, 0);
        var9.addVertexWithUV(guiLeft		, guiTop		, (double)this.zLevel, 0	, 0);
        var9.draw();
        
        //Bottom Left
        var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(guiLeft		, bottom		, (double)this.zLevel, 0	, 1);
        var9.addVertexWithUV(guiLeft + 15	, bottom		, (double)this.zLevel, 0.33	, 1);
        var9.addVertexWithUV(guiLeft + 15	, bottom - 15	, (double)this.zLevel, 0.33	, 0.66);
        var9.addVertexWithUV(guiLeft		, bottom - 15	, (double)this.zLevel, 0	, 0.66);
        var9.draw();

        //Bottom Right
        var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(right - 15	, bottom			, (double)this.zLevel, 0.66	, 1);
        var9.addVertexWithUV(right		, bottom			, (double)this.zLevel, 1	, 1);
        var9.addVertexWithUV(right		, bottom - 15		, (double)this.zLevel, 1	, 0.66);
        var9.addVertexWithUV(right - 15	, bottom - 15		, (double)this.zLevel, 0.66	, 0.66);
        var9.draw();

        //Top Right
        var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(right - 15	, guiTop + 15			, (double)this.zLevel, 0.66	, 0.33);
        var9.addVertexWithUV(right		, guiTop + 15			, (double)this.zLevel, 1	, 0.33);
        var9.addVertexWithUV(right		, guiTop				, (double)this.zLevel, 1	, 0);
        var9.addVertexWithUV(right - 15	, guiTop				, (double)this.zLevel, 0.66	, 0);
        var9.draw();

        //Center
        var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(guiLeft + 15	, bottom - 15		, (double)this.zLevel, 0.33	, 0.66);
        var9.addVertexWithUV(right - 15		, bottom - 15		, (double)this.zLevel, 0.66	, 0.66);
        var9.addVertexWithUV(right - 15		, guiTop + 15		, (double)this.zLevel, 0.66	, 0.33);
        var9.addVertexWithUV(guiLeft + 15	, guiTop + 15		, (double)this.zLevel, 0.33	, 0.33);
        var9.draw();
		
	}
}
