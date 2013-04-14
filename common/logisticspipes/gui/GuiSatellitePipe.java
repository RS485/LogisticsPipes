/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui;

import logisticspipes.interfaces.IGuiIDHandlerProvider;
import logisticspipes.logic.BaseLogicSatellite;
import logisticspipes.network.GuiIDs;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import org.lwjgl.opengl.GL11;

public class GuiSatellitePipe extends GuiContainer implements IGuiIDHandlerProvider {
	
	private BaseLogicSatellite _satellite;
	private EntityPlayer _player;
	
	public GuiSatellitePipe(BaseLogicSatellite satellite, EntityPlayer player){
		super(new Container(){
			@Override
			public boolean canInteractWith(EntityPlayer entityplayer) {
				return true;
			}
		});
		_satellite = satellite;
		_player = player;
		this.xSize = 116;
		this.ySize = 70;

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		
	
		buttonList.add(new GuiButton(0, (width / 2) - (30 /2) + 35, (height / 2) - (20 / 2), 30, 20, "+"));
		buttonList.add(new GuiButton(1, (width / 2) - (30 /2) - 35, (height / 2) - (20 / 2), 30, 20, "-"));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if(_satellite == null) return;
		if (guibutton.id == 0){
			_satellite.setNextId(_player);
		}
		
		if (guibutton.id == 1){
			_satellite.setPrevId(_player);
		}
		super.actionPerformed(guibutton);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		fontRenderer.drawString("Satellite ID", 33, 10, 0x404040);
		if(_satellite == null) return;
		fontRenderer.drawString(_satellite.satelliteId+"", 59 - fontRenderer.getStringWidth(_satellite.satelliteId+"")/2, 31, 0x404040);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture("/logisticspipes/gui/satellite.png");
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_SatelitePipe_ID;
	}

}
