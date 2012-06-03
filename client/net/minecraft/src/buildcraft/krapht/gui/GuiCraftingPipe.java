/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.gui;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.buildcraft.krapht.logic.LogicCrafting;
import net.minecraft.src.forge.MinecraftForgeClient;
import net.minecraft.src.krapht.gui.DummyContainer;
import net.minecraft.src.krapht.gui.SmallGuiButton;

import org.lwjgl.opengl.GL11;

public class GuiCraftingPipe extends GuiContainer {

	private final LogicCrafting _logic;
	
	public GuiCraftingPipe(InventoryPlayer inventoryplayer, IInventory dummyInventory, LogicCrafting logic) {
		super(null);
		
		DummyContainer dummy = new DummyContainer(inventoryplayer, dummyInventory);
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
	}

	@Override
	public void initGui() {
		super.initGui();
		
		controlList.add(new SmallGuiButton(0, (width-xSize) / 2 + 164, (height - ySize) / 2 + 50, 10,10, ">"));
		controlList.add(new SmallGuiButton(1, (width-xSize) / 2 + 129, (height - ySize) / 2 + 50, 10,10, "<"));
		controlList.add(new SmallGuiButton(2, (width-xSize) / 2 + 138, (height - ySize) / 2 + 75, 30,10, "Paint"));
		controlList.add(new SmallGuiButton(3, (width-xSize) / 2 + 47, (height - ySize) / 2 + 50, 37,10, "Import"));
	}
	
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch(guibutton.id){
		case 0:
			_logic.setNextSatellite();
			return;
		case 1: 
			_logic.setPrevSatellite();
			return;
		case 2:
			_logic.paintPathToSatellite();
			return;
		case 3:
			_logic.importFromCraftingTable();
			return;
		default:
			super.actionPerformed(guibutton);
			return;
		}
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		inventorySlots.onCraftGuiClosed(mc.thePlayer); // TODO NullPointerException thePlayer == null
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
//		int i = mc.renderEngine.getTexture("/net/minecraft/src/buildcraft/krapht/gui/crafting.png");
//		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
//		mc.renderEngine.bindTexture(i);
//		drawTexturedModalRect(0, 0, 0, 0, xSize, ySize);

		fontRenderer.drawString("Inputs", 18, 7, 0x404040);
		fontRenderer.drawString("Output", 48, 67, 0x404040);
		fontRenderer.drawString("Inventory", 18, 86, 0x404040);
		fontRenderer.drawString("Satellite", 132, 7, 0x404040);
		
		
		if (_logic.SatelliteId == 0){
			fontRenderer.drawString("Off", 144, 52, 0x404040);
			return;
		}
		if (_logic.isSatelliteConnected()){
			MinecraftForgeClient.bindTexture(core_LogisticsPipes.LOGISTICSPIPE_ROUTED_TEXTURE_FILE);
		}else{
			MinecraftForgeClient.bindTexture(core_LogisticsPipes.LOGISTICSPIPE_NOTROUTED_TEXTURE_FILE);
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(155, 50, 10 * (xSize / 16) , 0, 10, 10);
		MinecraftForgeClient.unbindTexture();
		fontRenderer.drawString(""+_logic.SatelliteId , 155 - fontRenderer.getStringWidth(""+_logic.SatelliteId) , 52, 0x404040);

		
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine.getTexture("/net/minecraft/src/buildcraft/krapht/gui/crafting.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
//		//
//		//mc.renderEngine.bindTexture(mod_LogisticsPipes.)
//		//mc.renderEngine.bindTexture(BuildCraftCore.redLaserTexture);
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
		
		
		drawRect(400, 400, 0, 0, 0x404040);
		
	}

}
