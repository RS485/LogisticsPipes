/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui;

import logisticspipes.interfaces.IGuiIDHandlerProvider;
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.main.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.SmallGuiButton;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Slot;

import org.lwjgl.opengl.GL11;

public class GuiCraftingPipe extends GuiContainer implements IGuiIDHandlerProvider {

	private final BaseLogicCrafting _logic;
	private final EntityPlayer _player;
	private final GuiButton[] buttonarray;
	
	public GuiCraftingPipe(EntityPlayer player, IInventory dummyInventory, BaseLogicCrafting logic) {
		super(null);
		_player = player;
		DummyContainer dummy = new DummyContainer(player.inventory, dummyInventory);
		dummy.addNormalSlotsForPlayerInventory(18, 97);

		//Input slots
        for(int l = 0; l < 9; l++) {
        	dummy.addDummySlot(l, 18 + l * 18, 18);
        }
        
        //Output slot
        dummy.addDummySlot(9, 90, 64);
		
        this.inventorySlots = dummy;
		_logic = logic;
		xSize = 195;
		ySize = 187;
		buttonarray = new GuiButton[6];
	}

	@Override
	public void initGui() {
		super.initGui();
		
		controlList.add(new SmallGuiButton(0, (width-xSize) / 2 + 164, (height - ySize) / 2 + 50, 10,10, ">"));
		controlList.add(new SmallGuiButton(1, (width-xSize) / 2 + 129, (height - ySize) / 2 + 50, 10,10, "<"));
		//controlList.add(new SmallGuiButton(2, (width-xSize) / 2 + 138, (height - ySize) / 2 + 75, 30,10, "Paint"));
		controlList.add(new SmallGuiButton(3, (width-xSize) / 2 + 47, (height - ySize) / 2 + 50, 37,10, "Import"));
		controlList.add(new SmallGuiButton(4, (width-xSize) / 2 + 15, (height - ySize) / 2 + 50, 28,10, "Open"));
		for(int i = 0; i < 6; i++) {
			controlList.add(buttonarray[i] = new SmallGuiButton(5 + i, (width-xSize) / 2 + 20 + 18 * i, (height - ySize) / 2 + 37, 10,10, ">"));
			buttonarray[i].drawButton = false;
		}
	}
	
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if(5 <= guibutton.id && guibutton.id < 11) {
			_logic.handleStackMove(guibutton.id - 5);
		}
		switch(guibutton.id){
		case 0:
			_logic.setNextSatellite(_player);
			return;
		case 1: 
			_logic.setPrevSatellite(_player);
			return;
		case 2:
			_logic.paintPathToSatellite();
			return;
		case 3:
			_logic.importFromCraftingTable(_player);
			return;
		case 4:
			_logic.openAttachedGui(_player);
			return;
		default:
			super.actionPerformed(guibutton);
			return;
		}
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		inventorySlots.onCraftGuiClosed(_player); // Fix approved
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		fontRenderer.drawString("Inputs", 18, 7, 0x404040);
		fontRenderer.drawString("Output", 48, 67, 0x404040);
		fontRenderer.drawString("Inventory", 18, 86, 0x404040);
		fontRenderer.drawString("Satellite", 132, 7, 0x404040);
		
		
		if (_logic.satelliteId == 0){
			fontRenderer.drawString("Off", 144, 52, 0x404040);
			return;
		}
		/*
		if (_logic.isSatelliteConnected()){
			MinecraftForgeClient.bindTexture(mod_LogisticsPipes.LOGISTICSPIPE_ROUTED_TEXTURE_FILE);
		}else{
			MinecraftForgeClient.bindTexture(mod_LogisticsPipes.LOGISTICSPIPE_NOTROUTED_TEXTURE_FILE);
		}*/
		//TODO /\ /\ ???

		//GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		//drawRect(0,1000,0,10000, 0xFFFF0000);
		//drawTexturedModalRect(155, 50, 10 * (xSize / 16) , 0, 10, 10);
		//MinecraftForgeClient.unbindTexture();
		fontRenderer.drawString(""+_logic.satelliteId , 155 - fontRenderer.getStringWidth(""+_logic.satelliteId) , 52, 0x404040);
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		super.drawScreen(par1, par2, par3);
	}
    
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine.getTexture("/logisticspipes/gui/crafting.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;

		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

		drawRect(400, 400, 0, 0, 0x404040);

		for(int count=36; count<42;count++) {
			Slot slot = inventorySlots.getSlot(count);
			if(slot != null && slot.getStack() != null && slot.getStack().getMaxStackSize() < 2) {
				drawRect(guiLeft + 18 + (18 * (count-36)), guiTop + 18, guiLeft + 18 + (18 * (count-36)) + 16, guiTop + 18 + 16, 0xFFFF0000);
				buttonarray[count - 36].drawButton = true;
			} else {
				buttonarray[count - 36].drawButton = false;
			}
		}
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_CRAFTINGPIPE_ID;
	}
}
