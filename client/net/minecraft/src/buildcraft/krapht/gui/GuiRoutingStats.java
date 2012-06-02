/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.gui;

import net.minecraft.src.buildcraft.krapht.CoreRoutedPipe;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.routing.IRouter;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.gui.KraphtBaseGuiScreen;

public class GuiRoutingStats extends KraphtBaseGuiScreen{ //GuiScreen{
	
	private IRouter _router;
	public GuiRoutingStats(IRouter router) {
		super(170, 200, 0, 0);
		_router = router;
	}
	
	@Override
	protected void keyTyped(char c, int i) {
		// TODO Auto-generated method stub
		if (c == 'e'){
			super.keyTyped(c, 1);
		}
		super.keyTyped(c, i);
	}
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		// TODO Auto-generated method stub
		super.keyTyped('z', 1);
	}
	
	@Override
	public void drawScreen(int i, int j, float f) {
		// TODO Auto-generated method stub
		
		super.drawGuiBackGround();
		
//		int colorLight = 0xFFC6C6C6;
//		int colorDark = 0xFF555555;
//		int colorWhite = 0xFFFFFFFF;
//		int colorBlack = 0xFF000000;
//				
//		int xSize = 170;
//		int ySize = 200;
//		
//		int left =  width/2 - xSize/2;
//		int top = height/2 - ySize/2 ;
//		
//		int right = width/2 + xSize/2;
//		int bottom = height/2 + ySize/2;
//		
//		int xCenter = (right + left) / 2;
//		int yCenter = (bottom + top) / 2;
//		
//		
//		//super.drawScreen(i, j, f);
//		drawRect(left + 2, top, right - 3, top+1, colorBlack);				// Top border
//		drawRect(left + 3, bottom-1, right - 2, bottom, colorBlack);		// Bottom border
//		drawRect(left, top + 2, left+1, bottom -3, colorBlack);				// Left border
//		drawRect(right-1, top + 3, right, bottom - 2, colorBlack);			// Right border
//		
//		drawRect(left+3, top + 3, right - 1, bottom -1, colorDark);			//Right/Bottom highlight
//		drawRect(left+1, top + 1, right - 3, bottom -3, colorWhite);		//Top/Left highlight
//		drawRect(left+3, top + 3, right - 3, bottom - 3, colorLight);		// Main background
//		
//		drawPoint(left + 1, top +1, colorBlack);							//Top-left border corner
//		drawPoint(left+3, top + 3, colorWhite);								//Top-left highlight corner
//		drawPoint(right - 3, top + 1, colorBlack);							//Top-right border corner
//		drawPoint(right - 2, top + 2, colorBlack);							//Top-right border corner
//		drawPoint(right - 3, top + 2, colorLight);							//Top-right highlight corner
//		
//		drawPoint(left + 1, bottom -3, colorBlack);							//Bottom-left border corner 1
//		drawPoint(left + 2, bottom -2, colorBlack);							//Bottom-left border corner 2
//		drawPoint(left + 2, bottom -3, colorLight);							//Bottom-left highlight corner
//		drawPoint(right - 2, bottom -2, colorBlack);						//Bottom-right border corner
//		drawPoint(right - 4, bottom - 4, colorDark);						//Bottom-right highlight corner

		
		String pipeName = ItemIdentifier.get(_router.getPipe().itemID, 0).getFriendlyName();
		fontRenderer.drawString(pipeName, xCenter - fontRenderer.getStringWidth(pipeName)/2, top + 10, 0x83601c);
		fontRenderer.drawString("Inbound:", left + 60 - fontRenderer.getStringWidth("Inbound:"), top + 25, 0x303030);
		fontRenderer.drawString("Outbound:", left + 60 - fontRenderer.getStringWidth("Outbound:"), top + 40, 0x303030);
		
		fontRenderer.drawString(_router.getInboundItemsCount()+"", left + 80 - fontRenderer.getStringWidth(_router.getInboundItemsCount()+"")/2, top + 25, 0x303030);
		fontRenderer.drawString(_router.getOutboundItemsCount()+"", left + 80 - fontRenderer.getStringWidth(_router.getOutboundItemsCount()+"")/2, top + 40, 0x303030);
		
		int sessionxCenter = left + 85;
		int lifetimexCenter = left + 130;
		
		fontRenderer.drawString("Session", sessionxCenter - fontRenderer.getStringWidth("Session") / 2, top + 55, 0x303030);
		fontRenderer.drawString("Lifetime", lifetimexCenter - fontRenderer.getStringWidth("Lifetime") / 2, top + 55, 0x303030);
		fontRenderer.drawString("Sent:", left + 60 - fontRenderer.getStringWidth("Sent:"), top + 65, 0x303030);
		fontRenderer.drawString("Recieved:", left + 60 - fontRenderer.getStringWidth("Recieved:"), top + 80, 0x303030);
		fontRenderer.drawString("Relayed:", left + 60 - fontRenderer.getStringWidth("Relayed:"), top + 95, 0x303030);
		CoreRoutedPipe pipe = _router.getPipe();
		fontRenderer.drawString(pipe.stat_session_sent+"", sessionxCenter - fontRenderer.getStringWidth(pipe.stat_session_sent+"")/2, top + 65, 0x303030);
		fontRenderer.drawString(pipe.stat_session_recieved+"", sessionxCenter - fontRenderer.getStringWidth(pipe.stat_session_recieved+"")/2, top + 80, 0x303030);
		fontRenderer.drawString(pipe.stat_session_relayed+"", sessionxCenter - fontRenderer.getStringWidth(pipe.stat_session_relayed+"")/2, top + 95, 0x303030);
		
		fontRenderer.drawString(pipe.stat_lifetime_sent+"", lifetimexCenter - fontRenderer.getStringWidth(pipe.stat_lifetime_sent+"")/2, top + 65, 0x303030);
		fontRenderer.drawString(pipe.stat_lifetime_recieved+"", lifetimexCenter - fontRenderer.getStringWidth(pipe.stat_lifetime_recieved+"")/2, top + 80, 0x303030);
		fontRenderer.drawString(pipe.stat_lifetime_relayed+"", lifetimexCenter - fontRenderer.getStringWidth(pipe.stat_lifetime_relayed+"")/2, top + 95, 0x303030);
		
		
		//fontRenderer.drawString("GameTime:" + mc.theWorld.getWorldTime(), left + 10, top + 85, 0x303030);

				
		String escString = "Press <ESC> to exit"; 
		fontRenderer.drawString(escString, xCenter - fontRenderer.getStringWidth(escString) / 2, bottom - 20, 0x404040);
			
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

}
