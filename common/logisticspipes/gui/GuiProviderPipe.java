/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui;

import logisticspipes.interfaces.IGuiIDHandlerProvider;
import logisticspipes.logic.LogicProvider;
import logisticspipes.main.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketCoordinates;
import logisticspipes.utils.gui.DummyContainer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.IInventory;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiProviderPipe extends GuiContainer implements IGuiIDHandlerProvider{
	private IInventory playerInventory;
	private IInventory dummyInventory;
	private LogicProvider logic; 
	
	public GuiProviderPipe(IInventory playerInventory, IInventory dummyInventory, LogicProvider logic) {
		super(null);
		
		DummyContainer dummy = new DummyContainer(playerInventory, dummyInventory);
		dummy.addNormalSlotsForPlayerInventory(18, 97);
		
		int xOffset = 72;
		int yOffset = 18;
		
		for (int row = 0; row < 3; row++){
			for (int column = 0; column < 3; column++){
				dummy.addDummySlot(column + row * 3, xOffset + column * 18, yOffset + row * 18);					
			}
		}
		this.inventorySlots = dummy;
		
		this.playerInventory = playerInventory;
		this.dummyInventory = dummyInventory;
		this.logic = logic;
		xSize = 194;
		ySize = 186;
		
	}
	
	@Override
	public void initGui() {
		super.initGui();
       controlList.clear();
       controlList.add(new GuiButton(0, width / 2 + 40, height / 2 - 59, 45, 20, logic.isExcludeFilter() ? "Exclude" : "Include"));
       controlList.add(new GuiButton(1, width / 2 - 90, height / 2 - 41, 38, 20, "Switch"));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 0){
			logic.setFilterExcluded(!logic.isExcludeFilter());
			((GuiButton)controlList.get(0)).displayString = logic.isExcludeFilter() ? "Exclude" : "Include";
			PacketDispatcher.sendPacketToServer(new PacketCoordinates(NetworkConstants.PROVIDER_PIPE_CHANGE_INCLUDE, logic.xCoord, logic.yCoord, logic.zCoord).getPacket());
		} else if (guibutton.id  == 1){
			logic.nextExtractionMode();
			PacketDispatcher.sendPacketToServer(new PacketCoordinates(NetworkConstants.PROVIDER_PIPE_NEXT_MODE, logic.xCoord, logic.yCoord, logic.zCoord).getPacket());
		}
		super.actionPerformed(guibutton);
	}
	
	public void refreshInclude() {
		((GuiButton)controlList.get(0)).displayString = logic.isExcludeFilter() ? "Exclude" : "Include";
	}
	
	private String getExtractionModeString(){
		switch(logic.getExtractionMode()){
			case Normal:
				return "Normal";
			case LeaveFirst:
				return "Leave 1st stack";
			case LeaveLast: 
				return "Leave last stack";
			case LeaveFirstAndLast:
				return "Leave first & last stack";
			case Leave1PerStack:
				return "Leave 1 item per stack";
			default:
				return "Unknown!";
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer() {
		fontRenderer.drawString(dummyInventory.getInvName(), xSize / 2 - fontRenderer.getStringWidth(dummyInventory.getInvName())/2, 6, 0x404040);
		fontRenderer.drawString("Inventory", 18, ySize - 102, 0x404040);
		fontRenderer.drawString("Mode: " + getExtractionModeString(), 9, ySize - 112, 0x404040);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine.getTexture("/logisticspipes/gui/supplier.png");

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_ProviderPipe_ID;
	}
	
}
