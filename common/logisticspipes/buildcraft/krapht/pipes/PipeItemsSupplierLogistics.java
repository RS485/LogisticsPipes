/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.buildcraft.krapht.pipes;

import logisticspipes.mod_LogisticsPipes;
import logisticspipes.buildcraft.krapht.IRequestItems;
import logisticspipes.buildcraft.krapht.RoutedPipe;
import logisticspipes.buildcraft.krapht.logic.LogicSupplier;
import logisticspipes.buildcraft.logisticspipes.modules.ILogisticsModule;

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
