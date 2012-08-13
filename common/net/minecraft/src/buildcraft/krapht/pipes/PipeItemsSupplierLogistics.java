/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.pipes;

import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.krapht.IRequestItems;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.logic.LogicSupplier;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;

public class PipeItemsSupplierLogistics extends RoutedPipe implements IRequestItems{

	private boolean _lastRequestFailed = false;
		
	public PipeItemsSupplierLogistics(int itemID) {
		super(new LogicSupplier(), itemID);
	}
	
	@Override
	public int getCenterTexture() {
		return mod_LogisticsPipes.LOGISTICSPIPE_SUPPLIER_TEXTURE;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
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
		return null;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}
}
