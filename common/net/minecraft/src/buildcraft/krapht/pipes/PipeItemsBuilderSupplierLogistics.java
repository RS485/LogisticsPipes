/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.pipes;

import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.mod_LogisticsPipes;
//import net.minecraft.src.buildcraft.api.Trigger;
//import net.minecraft.src.buildcraft.api.TriggerParameter;
import net.minecraft.src.buildcraft.krapht.IRequestItems;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
//import net.minecraft.src.buildcraft.krapht.TriggerSupplierFailed;
import net.minecraft.src.buildcraft.krapht.logic.LogicBuilderSupplier;
import net.minecraft.src.buildcraft.krapht.logic.LogicSupplier;
import net.minecraft.src.buildcraft.krapht.routing.Router;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.krapht.InventoryUtilFactory;

public class PipeItemsBuilderSupplierLogistics extends RoutedPipe implements IRequestItems{

	private InventoryUtilFactory _inventoryUtilFactory = new InventoryUtilFactory();

	private boolean _lastRequestFailed = false;
		
	public PipeItemsBuilderSupplierLogistics(int itemID) {
		super(new LogicBuilderSupplier(), itemID);
	}
	
	public PipeItemsBuilderSupplierLogistics(int itemID, InventoryUtilFactory inventoryUtilFactory) {
		this(itemID);		
		_inventoryUtilFactory = inventoryUtilFactory;
	}
	
	@Override
	public int getCenterTexture() {
		return mod_LogisticsPipes.LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE;
	}
	
	 /* TRIGGER INTERFACE */
	 public boolean isRequestFailed(){
		 return _lastRequestFailed;
	 }
	 
	 public void setRequestFailed(boolean value){
		 _lastRequestFailed = value;
	 }

	@Override
	public ILogisticsModule getLogisticsModule() {
		// TODO Auto-generated method stub
		return null;
	}
}
