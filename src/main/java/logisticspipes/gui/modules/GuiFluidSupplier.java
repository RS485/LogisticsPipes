/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui.modules;

import logisticspipes.modules.ModuleFluidSupplier;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.utils.gui.DummyContainer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiFluidSupplier extends GuiWithPreviousGuiContainer {

	private final ModuleFluidSupplier _liquidSupplier;
	
	
	public GuiFluidSupplier(IInventory playerInventory, CoreRoutedPipe pipe, ModuleFluidSupplier liquidSupplier, GuiScreen previousGui) {
		super(null,pipe,previousGui);
		_liquidSupplier = liquidSupplier;
		DummyContainer dummy = new DummyContainer(playerInventory, _liquidSupplier.getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(8, 60);

		//Pipe slots
	    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
	    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
	    }
	    
	    this.inventorySlots = dummy;
		xSize = 175;
		ySize = 142;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRenderer.drawString(_liquidSupplier.getFilterInventory().getInvName(), 8, 6, 0x404040);
		fontRenderer.drawString("Inventory", 8, ySize - 92, 0x404040);
	}
	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/itemsink.png");	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {	
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Module_FluidSupplier_ID;
	}
}
