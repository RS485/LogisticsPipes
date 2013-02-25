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
import logisticspipes.network.GuiIDs;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.SmallGuiButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class GuiCraftingPipe extends GuiContainer implements IGuiIDHandlerProvider {

	private final BaseLogicCrafting _logic;
	private final EntityPlayer _player;
	private final GuiButton[] buttonarray;
	private final GuiButton[] normalButtonArray;
	private final GuiButton[][] advancedSatButtonArray;
	private final boolean isAdvancedSat;
	private final boolean isSplitedCrafting;
	
	public GuiCraftingPipe(EntityPlayer player, IInventory dummyInventory, BaseLogicCrafting logic, boolean isAdvancedSat, boolean isSplitedCrafting) {
		super(null);
		_player = player;
		this.isAdvancedSat = isAdvancedSat;
		this.isSplitedCrafting = isSplitedCrafting;
		
		if(!isAdvancedSat && !isSplitedCrafting) {
			xSize = 177;
			ySize = 187;
		} else {
			xSize = 177;
			ySize = 187 + 30;
		}
		
		DummyContainer dummy = new DummyContainer(player.inventory, dummyInventory);
		dummy.addNormalSlotsForPlayerInventory(8, ySize - 82);

		//Input slots
        for(int l = 0; l < 9; l++) {
        	dummy.addDummySlot(l, 8 + l * 18, 18);
        }

		//Output slot
        if(!isAdvancedSat) {
        	dummy.addDummySlot(9, 85, 55);
        } else {
        	dummy.addDummySlot(9, 85, 105);
        }
		
        this.inventorySlots = dummy;
		_logic = logic;
		buttonarray = new GuiButton[6];
		normalButtonArray = new GuiButton[6];
		advancedSatButtonArray = new GuiButton[9][2];
		for(int i=0;i<9;i++) {
			advancedSatButtonArray[i] = new GuiButton[2];
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		
		if(!isAdvancedSat) {
			controlList.add(normalButtonArray[0] = new SmallGuiButton(0, (width-xSize) / 2 + 155, (height - ySize) / 2 + 50, 10,10, ">"));
			controlList.add(normalButtonArray[1] = new SmallGuiButton(1, (width-xSize) / 2 + 120, (height - ySize) / 2 + 50, 10,10, "<"));
		}
		if(!isAdvancedSat) {
			controlList.add(normalButtonArray[2] = new SmallGuiButton(3, (width-xSize) / 2 + 39, (height - ySize) / 2 + 50, 37,10, "Import"));
			controlList.add(normalButtonArray[3] = new SmallGuiButton(4, (width-xSize) / 2 + 6, (height - ySize) / 2 + 50, 28,10, "Open"));
			for(int i = 0; i < 6; i++) {
				controlList.add(buttonarray[i] = new SmallGuiButton(5 + i, (width-xSize) / 2 + 11 + 18 * i, (height - ySize) / 2 + 35, 10,10, ">"));
				buttonarray[i].drawButton = false;
			}
			controlList.add(normalButtonArray[4] = new SmallGuiButton(20, (width-xSize) / 2 + 155, (height - ySize) / 2 + 85, 10,10, ">"));
			controlList.add(normalButtonArray[5] = new SmallGuiButton(21, (width-xSize) / 2 + 120, (height - ySize) / 2 + 85, 10,10, "<"));
		} else {
			for(int i=0;i<9;i++) {
				controlList.add(advancedSatButtonArray[i][0] = new SmallGuiButton(30 + i, (width-xSize) / 2 + 10 + 18 * i, (height - ySize) / 2 + 40, 15,10, "/\\"));
				controlList.add(advancedSatButtonArray[i][1] = new SmallGuiButton(40 + i, (width-xSize) / 2 + 10 + 18 * i, (height - ySize) / 2 + 70, 15,10, "\\/"));
			}
			controlList.add(normalButtonArray[2] = new SmallGuiButton(3, (width-xSize) / 2 + 39, (height - ySize) / 2 + 100, 37,10, "Import"));
			controlList.add(normalButtonArray[3] = new SmallGuiButton(4, (width-xSize) / 2 + 6, (height - ySize) / 2 + 100, 28,10, "Open"));
			controlList.add(normalButtonArray[4] = new SmallGuiButton(20, (width-xSize) / 2 + 155, (height - ySize) / 2 + 105, 10,10, ">"));
			controlList.add(normalButtonArray[5] = new SmallGuiButton(21, (width-xSize) / 2 + 120, (height - ySize) / 2 + 105, 10,10, "<"));
		}
	}
	
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if(5 <= guibutton.id && guibutton.id < 11) {
			_logic.handleStackMove(guibutton.id - 5);
		}
		if(30 <= guibutton.id && guibutton.id < 40) {
			_logic.setNextSatellite(_player, guibutton.id - 30);
		}
		if(40 <= guibutton.id && guibutton.id < 50) {
			_logic.setPrevSatellite(_player, guibutton.id - 40);
		}
		switch(guibutton.id){
		case 0:
			_logic.setNextSatellite(_player);
			return;
		case 1: 
			_logic.setPrevSatellite(_player);
			return;
		/*case 2:
			_logic.paintPathToSatellite();
			return;*/
		case 3:
			_logic.importFromCraftingTable(_player);
			return;
		case 4:
			_logic.openAttachedGui(_player);
			return;
		case 20:
			_logic.priorityUp(_player);
			return;
		case 21:
			_logic.priorityDown(_player);
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
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRenderer.drawString("Inputs", 18, 7, 0x404040);
		fontRenderer.drawString("Inventory", 10, ySize - 93, 0x404040);
		
		if(!isAdvancedSat) {
			fontRenderer.drawString("Output", 77, 40, 0x404040);
			fontRenderer.drawString("Satellite", 123, 7, 0x404040);
			if (_logic.satelliteId == 0) {
				fontRenderer.drawString("Off", 135, 52, 0x404040);
			} else {
				fontRenderer.drawString(""+_logic.satelliteId , 146 - fontRenderer.getStringWidth(""+_logic.satelliteId) , 52, 0x404040);
			}
			fontRenderer.drawString("Priority:" , 123 , 75, 0x404040);
			fontRenderer.drawString(""+_logic.priority , 143 - (fontRenderer.getStringWidth(""+_logic.priority) / 2) , 87, 0x404040);
		} else {
			for(int i=0; i<9;i++) {
				if (_logic.advancedSatelliteIdArray[i] == 0) {
					fontRenderer.drawString("Off", 10 + (i * 18), 57, 0x404040);
				} else {
					fontRenderer.drawString(""+_logic.advancedSatelliteIdArray[i] , 20 - fontRenderer.getStringWidth(""+_logic.advancedSatelliteIdArray[i]) + (i * 18), 57, 0x404040);
				}
			}
			fontRenderer.drawString("Output", 77, 90, 0x404040);
			fontRenderer.drawString("Priority:" , 123 , 95, 0x404040);
			fontRenderer.drawString(""+_logic.priority , 143 - (fontRenderer.getStringWidth(""+_logic.priority) / 2) , 107, 0x404040);
		}
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		super.drawScreen(par1, par2, par3);
	}
    
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, zLevel, true, true, true, true, true);

		if(!isAdvancedSat) {
			drawRect(guiLeft + 115, guiTop + 4, guiLeft + 170, guiTop + 70, 0xff8B8B8B);
		}
		
		for(int i=0; i<9;i++) {
			BasicGuiHelper.drawSlotBackground(mc, guiLeft + 7 + (18*i), guiTop + 17);
		}
		if(!isAdvancedSat) {
			BasicGuiHelper.drawBigSlotBackground(mc, guiLeft + 80, guiTop + 50);
		} else {
			BasicGuiHelper.drawBigSlotBackground(mc, guiLeft + 80, guiTop + 100);
		}
		BasicGuiHelper.drawPlayerInventoryBackground(mc, guiLeft + 8, guiTop + ySize - 82);
		
		if(!isAdvancedSat) {
			for(int count=36; count<42;count++) {
				Slot slot = inventorySlots.getSlot(count);
				if(slot != null && slot.getStack() != null && slot.getStack().getMaxStackSize() < 2) {
					drawRect(guiLeft + 8 + (18 * (count-36)), guiTop + 18, guiLeft + 8 + (18 * (count-36)) + 16, guiTop + 18 + 16, 0xFFFF0000);
					buttonarray[count - 36].drawButton = true;
				} else {
					buttonarray[count - 36].drawButton = false;
				}
			}
		}
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_CRAFTINGPIPE_ID;
	}
}
