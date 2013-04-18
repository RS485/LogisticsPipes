/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logic.LogicBuilderSupplier;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;

public class PipeItemsBuilderSupplierLogistics extends CoreRoutedPipe implements IRequestItems{

	private boolean _lastRequestFailed = false;
		
	public PipeItemsBuilderSupplierLogistics(int itemID) {
		super(new LogicBuilderSupplier(), itemID);
		((LogicBuilderSupplier)logic)._power = this;
	}
	
	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_BUILDERSUPPLIER_TEXTURE;
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
	
	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public boolean hasGenericInterests() {
		return true;
	}

}
