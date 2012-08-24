/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.buildcraft.logisticspipes.modules;

import logisticspipes.buildcraft.krapht.GuiIDs;
import logisticspipes.buildcraft.krapht.logic.BaseRoutingLogic;
import logisticspipes.buildcraft.logisticspipes.modules.ModulePassiveSupplier;
import logisticspipes.krapht.gui.DummyContainer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.IInventory;
import net.minecraft.src.ModLoader;
import buildcraft.transport.Pipe;

import org.lwjgl.opengl.GL11;

public class GuiPassiveSupplier extends GuiWithPreviousGuiContainer {

	private final IInventory _playerInventory;
	private final ModulePassiveSupplier _supplier;
	
	
	public GuiPassiveSupplier(IInventory playerInventory, Pipe pipe, ModulePassiveSupplier supplier, GuiScreen previousGui) {
		super(null,pipe,previousGui);
		_supplier = supplier;
		DummyContainer dummy = new DummyContainer(playerInventory, _supplier.getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(8, 60);

		//Pipe slots
	    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
	    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
	    }
	    
	    this.inventorySlots = dummy;
		this._playerInventory = playerInventory;
		xSize = 175;
		ySize = 142;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer() {
		fontRenderer.drawString(_supplier.getFilterInventory().getInvName(), 8, 6, 0x404040);
		fontRenderer.drawString("Inventory", 8, ySize - 92, 0x404040);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine.getTexture("/logisticspipes/gui/itemsink.png");
				
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Module_PassiveSupplier_ID;
	}
}
