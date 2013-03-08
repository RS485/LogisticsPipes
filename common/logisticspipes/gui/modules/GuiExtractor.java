/** 
 * Copyright (c) Krapht, 2012
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui.modules;

import logisticspipes.interfaces.ISneakyDirectionReceiver;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import buildcraft.transport.Pipe;

public class GuiExtractor extends GuiWithPreviousGuiContainer {

	//private final SneakyPipe _pipe;
	
	private final ISneakyDirectionReceiver _orientationReceiver;
	private int slot;
	
	public GuiExtractor(IInventory playerInventory, Pipe pipe, ISneakyDirectionReceiver orientationReceiver, GuiScreen previousGui, int slot) {
		super(new DummyContainer(playerInventory, null),pipe,previousGui);
		_orientationReceiver = orientationReceiver;
		xSize = 160;
		ySize = 200;
		this.slot = slot;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();

		int left = width / 2 - xSize / 2;
		int top = height / 2 - ySize / 2;
		  
		controlList.add(new GuiButton(0, left + 73, top + 23, 20, 20, ""));	//YNEG
		controlList.add(new GuiButton(1, left + 73, top + 43, 20, 20, ""));	//YPOS
		controlList.add(new GuiButton(2, left + 73, top + 63, 20, 20, "")); //ZNEG
		controlList.add(new GuiButton(3, left+10, top + 43, 20, 20, ""));	//DEFAULT

		refreshButtons();
	}
	
	private void refreshButtons(){
		for (Object p : controlList){
			GuiButton button = (GuiButton) p;
			switch (button.id){
				case 0: button.displayString =  isExtract(ForgeDirection.UP); break;
				case 1: button.displayString =  isExtract(ForgeDirection.SOUTH); break;
				case 2: button.displayString =  isExtract(ForgeDirection.DOWN); break;
				case 3: button.displayString =  isExtract(ForgeDirection.UNKNOWN); break;
			}
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch (guibutton.id){
		case 0:
			_orientationReceiver.setSneakyDirection(ForgeDirection.UP);
			break;
		
		case 1:
			_orientationReceiver.setSneakyDirection(ForgeDirection.SOUTH);
			break;
		
		case 2:
			_orientationReceiver.setSneakyDirection(ForgeDirection.DOWN);
			break;
			
		case 3:
			_orientationReceiver.setSneakyDirection(ForgeDirection.UNKNOWN);
			break;
		}
		
		if(slot != 20) {
			MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.EXTRACTOR_MODULE_DIRECTION_SET, pipe.xCoord, pipe.yCoord, pipe.zCoord, _orientationReceiver.getSneakyDirection().ordinal() + (slot * 10)).getPacket());
		} else {
			MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.EXTRACTOR_MODULE_DIRECTION_SET, 0, -1, _orientationReceiver.getZPos(), _orientationReceiver.getSneakyDirection().ordinal() + (slot * 10)).getPacket());	
		}
		
		refreshButtons();
		super.actionPerformed(guibutton);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		
		refreshButtons();
		
		super.drawGuiContainerForegroundLayer(par1, par2);
		
		fontRenderer.drawString("Extract orientation", xSize / 2 - fontRenderer.getStringWidth("Extract orientation") / 2 , 10, 0x404040);
		fontRenderer.drawString("Default", 35, 50, 0x404040);
		fontRenderer.drawString("Top", 100, 30, 0x404040);
		fontRenderer.drawString("Side", 100, 50, 0x404040);
		fontRenderer.drawString("Bottom", 100, 70, 0x404040);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine.getTexture("/logisticspipes/gui/extractor.png");
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = guiLeft;
		int k = guiTop;
		//drawRect(width/2 - xSize / 2, height / 2 - ySize /2, width/2 + xSize / 2, height / 2 + ySize /2, 0xFF404040);
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

	private String getButtonText(boolean checked){
		return checked ? "[X]" : "[ ]";
	}

	private String isExtract(ForgeDirection o){
		return getButtonText(o == _orientationReceiver.getSneakyDirection());
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Module_Extractor_ID;
	}
	
	public void setMode(ForgeDirection o) {
		_orientationReceiver.setSneakyDirection(o);
		refreshButtons();
	}
}
