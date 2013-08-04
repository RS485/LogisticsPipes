/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gates;

import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.textures.provider.LPActionTriggerIconProvider;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.Pipe;

public class TriggerSupplierFailed extends LPTrigger implements ITriggerPipe{

	public TriggerSupplierFailed(int id) {
		super(id,"LogisticsPipes.trigger.supplierFailed");
	}
	
	@Override
	public int getIconIndex() {
		return LPActionTriggerIconProvider.triggerSupplierFailedIconIndex;
	}
	
	@Override
	public String getDescription() {
		return "Supplier failed";
	}

	@Override
	public boolean isTriggerActive(Pipe pipe, ITriggerParameter parameter) {
		if (pipe instanceof PipeItemsSupplierLogistics) {
			PipeItemsSupplierLogistics supplier = (PipeItemsSupplierLogistics) pipe;
			return supplier.isRequestFailed();
		}
		if (pipe instanceof PipeItemsFluidSupplier) {
			PipeItemsFluidSupplier supplier = (PipeItemsFluidSupplier) pipe;
			return supplier.isRequestFailed();
		}
		return false;
	}

}
