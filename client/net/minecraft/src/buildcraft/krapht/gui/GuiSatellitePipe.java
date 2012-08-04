/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.gui;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.Slot;
import buildcraft.core.BuildCraftContainer;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.logic.LogicSatellite;
import net.minecraft.src.buildcraft.logisticspipes.modules.IGuiIDHandlerProvider;

import org.lwjgl.opengl.GL11;

public class GuiSatellitePipe extends GuiContainer implements IGuiIDHandlerProvider {
	
	private LogicSatellite _satellite;
	
	public GuiSatellitePipe(LogicSatellite satellite){
		super(new Container(){
			@Override
			public boolean canInteractWith(EntityPlayer entityplayer) {
				return true;
			}
		});
		_satellite = satellite;
		this.xSize = 116;
		this.ySize = 70;

	}
	
	@Override
	public void initGui() {
		super.initGui();
		
	
		controlList.add(new GuiButton(0, (width / 2) - (30 /2) + 35, (height / 2) - (20 / 2), 30, 20, "+"));
		controlList.add(new GuiButton(1, (width / 2) - (30 /2) - 35, (height / 2) - (20 / 2), 30, 20, "-"));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if(_satellite == null) return;
		if (guibutton.id == 0){
			_satellite.setNextId();
		}
		
		if (guibutton.id == 1){
			_satellite.setPrevId();
		}
		super.actionPerformed(guibutton);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer() {
		super.drawGuiContainerForegroundLayer();
		fontRenderer.drawString("Satellite ID", 33, 10, 0x404040);
		if(_satellite == null) return;
		fontRenderer.drawString(_satellite.satelliteId+"", 59 - fontRenderer.getStringWidth(_satellite.satelliteId+"")/2, 31, 0x404040);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine.getTexture("/logisticspipes/gui/satellite.png");
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_SatelitePipe_ID;
	}

}
