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
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.ticks.RenderTickHandler;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.SmallGuiButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public class GuiCraftingPipe extends GuiContainer implements IGuiIDHandlerProvider {

	private final PipeItemsCraftingLogistics _pipe;
	private final EntityPlayer _player;
	private final GuiButton[] buttonarray;
	private final GuiButton[] normalButtonArray;
	private final GuiButton[][] advancedSatButtonArray;
	private final GuiButton[][] liquidGuiParts;
	private final boolean isAdvancedSat;
	private final int liquidCrafter;
	private final boolean hasByproductExtractor;
	
	public GuiCraftingPipe(EntityPlayer player, IInventory dummyInventory, PipeItemsCraftingLogistics logic, boolean isAdvancedSat, int liquidCrafter, int[] amount, boolean hasByproductExtractor) {
		super(null);
		_player = player;
		this.isAdvancedSat = isAdvancedSat;
		this.liquidCrafter = liquidCrafter;
		this.hasByproductExtractor = hasByproductExtractor;
		
		if(!hasByproductExtractor) {
			xSize = 177;
		} else {
			xSize = 217;
		}
		
		if(!isAdvancedSat) {
			ySize = 187;
		} else {
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
		
        liquidGuiParts = new GuiButton[liquidCrafter][];
        
        for(int i=0;i<liquidCrafter;i++) {
			int liquidLeft = -(i*40) - 40;
			dummy.addFluidSlot(i, logic.getFluidInventory(), liquidLeft + 13, 22);
		}
        
        if(hasByproductExtractor) {
        	dummy.addDummySlot(10, 187, 105);
        }
        
        this.inventorySlots = dummy;
		_pipe = logic;
		_pipe.setFluidAmount(amount);
		buttonarray = new GuiButton[6];
		normalButtonArray = new GuiButton[8];
		advancedSatButtonArray = new GuiButton[9][2];
		for(int i=0;i<9;i++) {
			advancedSatButtonArray[i] = new GuiButton[2];
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		if(!isAdvancedSat) {
			buttonList.add(normalButtonArray[0] = new SmallGuiButton(0, (width-xSize) / 2 + 155, (height - ySize) / 2 + 50, 10,10, ">"));
			buttonList.add(normalButtonArray[1] = new SmallGuiButton(1, (width-xSize) / 2 + 120, (height - ySize) / 2 + 50, 10,10, "<"));
			buttonList.add(normalButtonArray[2] = new SmallGuiButton(3, (width-xSize) / 2 + 39, (height - ySize) / 2 + 50, 37,10, "Import"));
			buttonList.add(normalButtonArray[3] = new SmallGuiButton(4, (width-xSize) / 2 + 6, (height - ySize) / 2 + 50, 28,10, "Open"));
			for(int i = 0; i < 6; i++) {
				buttonList.add(buttonarray[i] = new SmallGuiButton(5 + i, (width-xSize) / 2 + 11 + 18 * i, (height - ySize) / 2 + 35, 10,10, ">"));
				buttonarray[i].drawButton = false;
			}
			buttonList.add(normalButtonArray[4] = new SmallGuiButton(20, (width-xSize) / 2 + 155, (height - ySize) / 2 + 85, 10,10, ">"));
			buttonList.add(normalButtonArray[5] = new SmallGuiButton(21, (width-xSize) / 2 + 120, (height - ySize) / 2 + 85, 10,10, "<"));
			if(liquidCrafter != 0) {
				buttonList.add(normalButtonArray[6] = new SmallGuiButton(22, guiLeft - (liquidCrafter * 40) / 2 + 5, guiTop + 158, 10,10, ">"));
				buttonList.add(normalButtonArray[7] = new SmallGuiButton(23, guiLeft - (liquidCrafter * 40) / 2 - 15, guiTop + 158, 10,10, "<"));	
			}
		} else {
			for(int i=0;i<9;i++) {
				buttonList.add(advancedSatButtonArray[i][0] = new SmallGuiButton(30 + i, (width-xSize) / 2 + 10 + 18 * i, (height - ySize) / 2 + 40, 15,10, "/\\"));
				buttonList.add(advancedSatButtonArray[i][1] = new SmallGuiButton(40 + i, (width-xSize) / 2 + 10 + 18 * i, (height - ySize) / 2 + 70, 15,10, "\\/"));
			}
			buttonList.add(normalButtonArray[2] = new SmallGuiButton(3, (width-xSize) / 2 + 39, (height - ySize) / 2 + 100, 37,10, "Import"));
			buttonList.add(normalButtonArray[3] = new SmallGuiButton(4, (width-xSize) / 2 + 6, (height - ySize) / 2 + 100, 28,10, "Open"));
			buttonList.add(normalButtonArray[4] = new SmallGuiButton(20, (width-xSize) / 2 + 155, (height - ySize) / 2 + 105, 10,10, ">"));
			buttonList.add(normalButtonArray[5] = new SmallGuiButton(21, (width-xSize) / 2 + 120, (height - ySize) / 2 + 105, 10,10, "<"));
		}
		for(int i=0;i<liquidCrafter;i++) {
			int liquidLeft = guiLeft - (i*40) - 40;
			liquidGuiParts[i] = new GuiButton[10];
			buttonList.add(liquidGuiParts[i][0] = new SmallGuiButton(100 + 10 * i + 0, liquidLeft + 22, guiTop +  65, 10,10, "+"));
			buttonList.add(liquidGuiParts[i][1] = new SmallGuiButton(100 + 10 * i + 1, liquidLeft + 22, guiTop +  85, 10,10, "+"));
			buttonList.add(liquidGuiParts[i][2] = new SmallGuiButton(100 + 10 * i + 2, liquidLeft + 22, guiTop + 105, 10,10, "+"));
			buttonList.add(liquidGuiParts[i][3] = new SmallGuiButton(100 + 10 * i + 3, liquidLeft + 22, guiTop + 125, 10,10, "+"));
			buttonList.add(liquidGuiParts[i][4] = new SmallGuiButton(100 + 10 * i + 4, liquidLeft +  8, guiTop +  65, 10,10, "-"));
			buttonList.add(liquidGuiParts[i][5] = new SmallGuiButton(100 + 10 * i + 5, liquidLeft +  8, guiTop +  85, 10,10, "-"));
			buttonList.add(liquidGuiParts[i][6] = new SmallGuiButton(100 + 10 * i + 6, liquidLeft +  8, guiTop + 105, 10,10, "-"));
			buttonList.add(liquidGuiParts[i][7] = new SmallGuiButton(100 + 10 * i + 7, liquidLeft +  8, guiTop + 125, 10,10, "-"));
			if(isAdvancedSat) {
				buttonList.add(liquidGuiParts[i][8] = new SmallGuiButton(100 + 10 * i + 8, liquidLeft +  5, guiTop + 158, 10,10, "<"));
				buttonList.add(liquidGuiParts[i][9] = new SmallGuiButton(100 + 10 * i + 9, liquidLeft +  25, guiTop + 158, 10,10, ">"));
			}
		}
	}
	
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if(5 <= guibutton.id && guibutton.id < 11) {
			_pipe.handleStackMove(guibutton.id - 5);
		}
		if(30 <= guibutton.id && guibutton.id < 40) {
			_pipe.setNextSatellite(_player, guibutton.id - 30);
		}
		if(40 <= guibutton.id && guibutton.id < 50) {
			_pipe.setPrevSatellite(_player, guibutton.id - 40);
		}
		if(100 <= guibutton.id && guibutton.id < 200) {
			int i = guibutton.id - 100;
			int action = i % 10;
			i -= action;
			i /= 10;
			if(action >=0 && action < 8) {
				int amount=0;
				switch(action) {
					case 0:amount = 1;     break;
					case 1:amount = 10;    break;
					case 2:amount = 100;   break;
					case 3:amount = 1000;  break;
					case 4:amount = -1;    break;
					case 5:amount = -10;   break;
					case 6:amount = -100;  break;
					case 7:amount = -1000; break;
					default:break;
				}
				_pipe.changeFluidAmount(amount, i, _player);
			} else if(action == 8) {
				_pipe.setPrevFluidSatellite(_player, i);
			} else if(action == 9) {
				_pipe.setNextFluidSatellite(_player, i);
			}
		}
		switch(guibutton.id){
		case 0:
			_pipe.setNextSatellite(_player);
			return;
		case 1: 
			_pipe.setPrevSatellite(_player);
			return;
		/*case 2:
			_logic.paintPathToSatellite();
			return;*/
		case 3:
			_pipe.importFromCraftingTable(_player);
			return;
		case 4:
			_pipe.openAttachedGui(_player);
			RenderTickHandler.addGuiToReopen(_pipe.getX(), _pipe.getY(), _pipe.getZ(), getGuiID());
			return;
		case 20:
			_pipe.priorityUp(_player);
			return;
		case 21:
			_pipe.priorityDown(_player);
			return;
		case 22:
			_pipe.setNextFluidSatellite(_player, -1);
			return;
		case 23:
			_pipe.setPrevFluidSatellite(_player, -1);
			return;
		default:
			super.actionPerformed(guibutton);
			return;
		}
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		inventorySlots.onContainerClosed(_player); // Fix approved
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRenderer.drawString("Inputs", 18, 7, 0x404040);
		fontRenderer.drawString("Inventory", 10, ySize - 93, 0x404040);
		
		if(!isAdvancedSat) {
			fontRenderer.drawString("Output", 77, 40, 0x404040);
			fontRenderer.drawString("Satellite", 123, 7, 0x404040);
			if (_pipe.satelliteId == 0) {
				fontRenderer.drawString("Off", 135, 52, 0x404040);
			} else {
				fontRenderer.drawString(""+_pipe.satelliteId , 146 - fontRenderer.getStringWidth(""+_pipe.satelliteId) , 52, 0x404040);
			}
			fontRenderer.drawString("Priority:" , 123 , 75, 0x404040);
			fontRenderer.drawString(""+_pipe.priority , 143 - (fontRenderer.getStringWidth(""+_pipe.priority) / 2) , 87, 0x404040);
		} else {
			for(int i=0; i<9;i++) {
				if (_pipe.advancedSatelliteIdArray[i] == 0) {
					fontRenderer.drawString("Off", 10 + (i * 18), 57, 0x404040);
				} else {
					fontRenderer.drawString(""+_pipe.advancedSatelliteIdArray[i] , 20 - fontRenderer.getStringWidth(""+_pipe.advancedSatelliteIdArray[i]) + (i * 18), 57, 0x404040);
				}
			}
			fontRenderer.drawString("Output", 77, 90, 0x404040);
			fontRenderer.drawString("Priority:" , 123 , 95, 0x404040);
			fontRenderer.drawString(""+_pipe.priority , 143 - (fontRenderer.getStringWidth(""+_pipe.priority) / 2) , 107, 0x404040);
		}
		
		for(int i=0;i<liquidCrafter;i++) {
			int liquidLeft = -(i*40) - 40;
			fontRenderer.drawString(Integer.toString(_pipe.getFluidAmount()[i]), liquidLeft + 21 - (fontRenderer.getStringWidth(Integer.toString(_pipe.getFluidAmount()[i])) / 2), 43, 0x404040);
			fontRenderer.drawString("1", liquidLeft + 18, 57, 0x404040);
			fontRenderer.drawString("10", liquidLeft + 15, 77, 0x404040);
			fontRenderer.drawString("100", liquidLeft + 12, 97, 0x404040);
			fontRenderer.drawString("1000", liquidLeft + 9, 117, 0x404040);
			if(isAdvancedSat) {
				if(_pipe.liquidSatelliteIdArray[i] == 0) {
					drawRect(liquidLeft + 1, 13, liquidLeft + 40, 142, 0xAA8B8B8B);
					fontRenderer.drawString("Off", liquidLeft + 13, 149, 0x404040);
					for(int j=0;j<8;j++) {
						liquidGuiParts[i][j].enabled = false;
					}
				} else {
					fontRenderer.drawString(Integer.toString(_pipe.liquidSatelliteIdArray[i]), liquidLeft + 21 - (fontRenderer.getStringWidth(Integer.toString(_pipe.liquidSatelliteIdArray[i])) / 2), 149, 0x404040);
					for(int j=0;j<8;j++) {
						liquidGuiParts[i][j].enabled = true;
					}
				}
			}
		}
		if(!isAdvancedSat && liquidCrafter != 0) {
			if(_pipe.liquidSatelliteId == 0) {
				drawRect(-(liquidCrafter * 40) + 1, 13, 0, 142, 0xAA8B8B8B);
				fontRenderer.drawString("Off", -(liquidCrafter * 40) / 2 - 7, 149, 0x404040);
				for(int i=0;i<liquidCrafter;i++) {
					for(int j=0;j<8;j++) {
						liquidGuiParts[i][j].enabled = false;
					}
				}
			} else {
				fontRenderer.drawString(Integer.toString(_pipe.liquidSatelliteId), -(liquidCrafter * 40) / 2 + 1 - (fontRenderer.getStringWidth(Integer.toString(_pipe.liquidSatelliteId)) / 2), 149, 0x404040);
				for(int i=0;i<liquidCrafter;i++) {
					for(int j=0;j<8;j++) {
						liquidGuiParts[i][j].enabled = true;
					}
				}
			}
		}
		for(int i=0;i<liquidCrafter;i++) {
			if(_pipe.getFluidInventory().getStackInSlot(i) == null && !((!isAdvancedSat && _pipe.liquidSatelliteId == 0) || (isAdvancedSat && _pipe.liquidSatelliteIdArray[i] == 0))) {
				drawRect(-((i + 1) * 40) + 1, 40, -(i * 40), 142, 0xAA8B8B8B);
				for(int j=0;j<8;j++) {
					liquidGuiParts[i][j].enabled = false;
				}
			}
		}
		if(hasByproductExtractor) {
			fontRenderer.drawString("Extra", xSize - 35, 88, 0x404040);
		}
	}
	
	@Override
	protected boolean isPointInRegion(int x, int y, int par3, int par4, int par5, int par6) {
		if(!isAdvancedSat && liquidCrafter != 0) {
			if(_pipe.liquidSatelliteId == 0) {
				if(-(liquidCrafter * 40) < x && x < 0) {
					if(10 < y && y < 170) {
						return false;
					}
				}
			}	
		} else if(liquidCrafter != 0) {
			for(int i=0;i<liquidCrafter;i++) {
				if(_pipe.liquidSatelliteIdArray[i] == 0) {
					if(-((i + 1) * 40) < x && x < -(i * 40)) {
						if(10 < y && y < 170) {
							return false;
						}
					}
				}	
				
			}
		}
		return super.isPointInRegion(x, y, par3, par4, par5, par6);
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		super.drawScreen(par1, par2, par3);
	}
    
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, guiLeft + xSize - (hasByproductExtractor ? 40:0), guiTop + ySize, zLevel, true, true, true, true, true);

		if(liquidCrafter != 0) {
			BasicGuiHelper.drawGuiBackGround(mc, guiLeft - (liquidCrafter * 40) - 2, guiTop + 10, guiLeft + 15, guiTop + 175, zLevel, true, true, true, true, false);
			if(liquidCrafter > 1) {
				for(int i=1;i < liquidCrafter;i++) {
					int xLine = guiLeft - (i * 40);
					drawRect(xLine, guiTop + 13, xLine + 1, guiTop + (isAdvancedSat ? 172 : 142), 0xff8B8B8B);
				}
			}
			
			if(!isAdvancedSat) {
				drawRect(guiLeft - (liquidCrafter * 40), guiTop + 142, guiLeft, guiTop + 143, 0xff8B8B8B);
			}
			
			for(int i=0;i<liquidCrafter;i++) {
				int liquidLeft = guiLeft - (i*40) - 40;
				BasicGuiHelper.drawSlotBackground(mc, liquidLeft + 12, guiTop + 21);
			}
		}
		
		if(hasByproductExtractor) {
			BasicGuiHelper.drawGuiBackGround(mc, guiLeft + xSize - 55, guiTop + 80, guiLeft + xSize, guiTop + 135, zLevel, true, true, false, true, true);
			BasicGuiHelper.drawBigSlotBackground(mc, guiLeft + xSize - 35, guiTop + 100);
		}
		
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
		
		/*
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
		*/
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_CRAFTINGPIPE_ID;
	}
}
