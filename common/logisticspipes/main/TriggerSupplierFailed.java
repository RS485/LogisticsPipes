/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.main;

import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.textures.Textures;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.Trigger;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.Pipe;

public class TriggerSupplierFailed extends Trigger implements ITriggerPipe{

	public TriggerSupplierFailed(int id) {
		super(id);
	}
	
	@Override
	public int getIndexInTexture() {
		return 0 * 16 + 0;
	}
	
	@Override
	public String getDescription() {
		return "Supplier failed";
	}

	@Override
	public boolean isTriggerActive(Pipe pipe, ITriggerParameter parameter) {
		if (!(pipe instanceof PipeItemsSupplierLogistics)) return false;
		PipeItemsSupplierLogistics supplier = (PipeItemsSupplierLogistics) pipe;
		return supplier.isRequestFailed();
	}

	@Override
	public String getTextureFile() {
		return Textures.LOGISTICSACTIONTRIGGERS_TEXTURE_FILE;
	}
}
