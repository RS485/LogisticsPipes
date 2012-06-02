///** 
// * Copyright (c) Krapht, 2011
// * 
// * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
// * License 1.0, or MMPL. Please check the contents of the license located in
// * http://www.mod-buildcraft.com/MMPL-1.0.txt
// */
//
//package net.minecraft.src.buildcraft.krapht.logic;
//
//import net.minecraft.src.EntityPlayer;
//import net.minecraft.src.ModLoader;
//import net.minecraft.src.NBTTagCompound;
//import net.minecraft.src.buildcraft.krapht.CoreRoutedPipe;
//import net.minecraft.src.buildcraft.krapht.gui.GuiLogisticsPipe;
//import net.minecraft.src.krapht.InventoryUtil;
//import net.minecraft.src.krapht.InventoryUtilFactory;
//import net.minecraft.src.krapht.ItemIdentifier;
//import net.minecraft.src.krapht.SimpleInventory;
//
//public class LogicBasic extends BaseRoutingLogic{
//
//	private SimpleInventory dummyInventory = new SimpleInventory(9, "Requested items", 127);
//	private final InventoryUtilFactory _invUtilFactory;
//	private final InventoryUtil _dummyInvUtil;
//
//	public boolean isDefaultRoute = false;
//	
//	
//	public LogicBasic(){
//		this(new InventoryUtilFactory());
//	}
//	
//	public LogicBasic(InventoryUtilFactory invUtilFactory){
//		_invUtilFactory = invUtilFactory;
//		_dummyInvUtil = _invUtilFactory.getInventoryUtil(dummyInventory);
//	}
//
//	public int RequestsItem(ItemIdentifier item) {
//		
//		if (!((CoreRoutedPipe)container.pipe).isEnabled()){
//			return 0;
//		}
//		
//		if (item == null){
//			return isDefaultRoute?1:0;
//		}
//		
//		return _dummyInvUtil.getItemCount(item);
//	}
//
//	@Override
//	public boolean canInteractWith(EntityPlayer entityplayer) {
//		// TODO Auto-generated method stub
//		return true;
//	}
//	
////	@Override
////	public void readFromNBT(NBTTagCompound nbttagcompound) {
////		super.readFromNBT(nbttagcompound);	
////		dummyInventory.readFromNBT(nbttagcompound, "");
////    	isDefaultRoute = nbttagcompound.getBoolean("defaultdestination");
////    }
//
////	@Override
////    public void writeToNBT(NBTTagCompound nbttagcompound) {
////    	super.writeToNBT(nbttagcompound);
////    	dummyInventory.writeToNBT(nbttagcompound, "");
////    	nbttagcompound.setBoolean("defaultdestination", isDefaultRoute);
////    }
//
//	@Override
//	public void destroy() {	}
//
//	@Override
//	public void onWrenchClicked(EntityPlayer entityplayer) {
//		ModLoader.getMinecraftInstance().displayGuiScreen(new GuiLogisticsPipe(entityplayer.inventory, dummyInventory, this));		
//	}
//}
