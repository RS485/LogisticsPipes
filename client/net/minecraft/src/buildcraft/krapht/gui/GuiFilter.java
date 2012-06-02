///** 
// * Copyright (c) Krapht, 2011
// * 
// * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
// * License 1.0, or MMPL. Please check the contents of the license located in
// * http://www.mod-buildcraft.com/MMPL-1.0.txt
// */
//
//package net.minecraft.src.buildcraft.krapht.gui;
//
//import org.lwjgl.opengl.GL11;
//
//import net.minecraft.src.EntityPlayer;
//import net.minecraft.src.GuiContainer;
//import net.minecraft.src.GuiScreen;
//import net.minecraft.src.IInventory;
//import net.minecraft.src.ModLoader;
//import net.minecraft.src.krapht.SimpleInventory;
//import net.minecraft.src.krapht.gui.DummyContainer;
//
//public class GuiFilter extends GuiContainer {
//	
//	private final EntityPlayer _player;
//	private final GuiContainer _previousGui;
//	
//	private final IInventory _playerInventory;
//	private final IInventory _filterInventory;
//
//	public GuiFilter(EntityPlayer player, GuiContainer prevGui, IInventory filterInventory) {
//		super(null);
//		_player = player;
//		_previousGui = prevGui;
//		
//		_playerInventory = _player.inventory;
//		_filterInventory = filterInventory;
//
//		
//		DummyContainer dummy = new DummyContainer(_player.inventory, filterInventory);
//		dummy.addNormalSlotsForPlayerInventory(9, 64);
//
//		//Pipe slots
//	    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
//	    	dummy.addDummySlot(pipeSlot, 9 + pipeSlot * 18, 21);
//	    }
//	    
//	    this.inventorySlots = dummy;
//		xSize = 180;
//		ySize = 150;
//	}
//	
//	@Override
//	protected void keyTyped(char c, int i) {
//		if (i == 1 || c == 'e'){
//			_previousGui.initGui();
//			ModLoader.OpenGUI(_player, _previousGui);
//		}
//	}
//
//	
//	@Override
//	protected void drawGuiContainerForegroundLayer() {
//		super.drawGuiContainerForegroundLayer();
//		fontRenderer.drawString(_filterInventory.getInvName(), 8, 8, 0x404040);
//		fontRenderer.drawString(_playerInventory.getInvName(), 8, 50, 0x404040);
//
//	}
//	
//	@Override
//	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
//		int i = mc.renderEngine.getTexture("/net/minecraft/src/buildcraft/krapht/gui/filter.png");
//		
//		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
//		mc.renderEngine.bindTexture(i);
//		int j = (width - xSize) / 2;
//		int k = (height - ySize) / 2;
//		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
//
//		///logisticspipes_3.x/logisticspipes_base/net/minecraft/src/buildcraft/krapht/gui/filter.png	
//	}
//
//}
