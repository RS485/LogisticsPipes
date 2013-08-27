/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui;

import logisticspipes.interfaces.IGuiIDHandlerProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiSatellitePipe extends GuiContainer implements IGuiIDHandlerProvider {

	private PipeItemsSatelliteLogistics _satellite;
	private PipeFluidSatellite _liquidSatellite;
	private EntityPlayer _player;

	public GuiSatellitePipe(PipeItemsSatelliteLogistics satellite, EntityPlayer player){
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
	
	public GuiSatellitePipe(PipeFluidSatellite satellite, EntityPlayer player){
		super(new Container(){
			@Override
			public boolean canInteractWith(EntityPlayer entityplayer) {
				return true;
			}
		});
		_liquidSatellite = satellite;
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
		if(_satellite != null) {
			if (guibutton.id == 0){
				_satellite.setNextId(_player);
			}
			
			if (guibutton.id == 1){
				_satellite.setPrevId(_player);
			}
			super.actionPerformed(guibutton);
		} else if(_liquidSatellite != null) {
			if (guibutton.id == 0){
				_liquidSatellite.setNextId(_player);
			}
			
			if (guibutton.id == 1){
				_liquidSatellite.setPrevId(_player);
			}
			super.actionPerformed(guibutton);
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		fontRenderer.drawString("Satellite ID", 33, 10, 0x404040);
		if(_satellite != null) {
			fontRenderer.drawString(_satellite.satelliteId+"", 59 - fontRenderer.getStringWidth(_satellite.satelliteId+"")/2, 31, 0x404040);
		}
		if(_liquidSatellite != null) {
			fontRenderer.drawString(_liquidSatellite.satelliteId+"", 59 - fontRenderer.getStringWidth(_liquidSatellite.satelliteId+"")/2, 31, 0x404040);
		}
	}
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/satellite.png");
	
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.func_110577_a(TEXTURE);
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_SatelitePipe_ID;
	}
}
