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
//import net.minecraft.src.GuiButton;
//import net.minecraft.src.GuiContainer;
//import net.minecraft.src.IInventory;
//import net.minecraft.src.buildcraft.krapht.logic.LogicBasic;
//import net.minecraft.src.krapht.gui.DummyContainer;
//
//import org.lwjgl.opengl.GL11;
//
///*
// * Most of the code in this class is taken from Buildcraft
// * /minecraft/src/buildcraft/transport/GuiDiamondPipe.java 
// * with only minor modifications so all credit goes to SpaceToad
// */
//public class GuiLogisticsPipe extends GuiContainer {
//
//	IInventory playerInventory;
//	IInventory dummyInventory;
//	LogicBasic logic;
//	
//	
//	@Override
//	public void initGui() {
//		super.initGui();
//       //Default item toggle:
//       controlList.clear();
//       controlList.add(new GuiButton(0, width / 2 + 50, height / 2 - 34, 30, 20, logic.isDefaultRoute ? "Yes" : "No"));
//	}
//	
//	@Override
//	protected void actionPerformed(GuiButton guibutton) {
//		switch(guibutton.id)
//		{
//			case 0:
//				logic.isDefaultRoute = !logic.isDefaultRoute;
//				((GuiButton)controlList.get(0)).displayString = logic.isDefaultRoute ? "Yes" : "No";
//				break;
//		}
//		
//	}
//	
//	public GuiLogisticsPipe(IInventory playerInventory, IInventory dummyInventory, LogicBasic logic) {
//		super(null);
//		
//		DummyContainer dummy = new DummyContainer(playerInventory, dummyInventory);
//		dummy.addNormalSlotsForPlayerInventory(8, 60);
//
//		//Pipe slots
//	    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
//	    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
//	    }
//	    
//	    this.inventorySlots = dummy;
//		this.logic = logic;
//		this.playerInventory = playerInventory;
//		this.dummyInventory = dummyInventory;
//		xSize = 175;
//		ySize = 142;
//	}
//	
//	@Override
//	protected void drawGuiContainerForegroundLayer() {
//		fontRenderer.drawString(dummyInventory.getInvName(), 8, 6, 0x404040);
//		fontRenderer.drawString(playerInventory.getInvName(), 8, ySize - 92, 0x404040);
//		fontRenderer.drawString("Default route:", 65, 45, 0x404040);
//	}
//	
//	@Override
//	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
//		int i = mc.renderEngine.getTexture("/net/minecraft/src/buildcraft/krapht/gui/logisticspipegui.png");
//				
//		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
//		mc.renderEngine.bindTexture(i);
//		int j = (width - xSize) / 2;
//		int k = (height - ySize) / 2;
//		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
//	}
//
//	//int inventoryRows = 1;
//}
