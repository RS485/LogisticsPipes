/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.proxy.buildcraft.gates;

import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.proxy.buildcraft.gates.wrapperclasses.PipeWrapper;
import logisticspipes.textures.provider.LPActionTriggerIconProvider;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.transport.IPipeTrigger;
import buildcraft.transport.Pipe;

public class TriggerSupplierFailed extends LPTrigger implements IPipeTrigger{

	public TriggerSupplierFailed() {
		super("LogisticsPipes:trigger.supplierFailed");
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
		if(pipe instanceof PipeWrapper) {
			if (((PipeWrapper)pipe).tile.pipe instanceof PipeItemsSupplierLogistics) {
				PipeItemsSupplierLogistics supplier = (PipeItemsSupplierLogistics) ((PipeWrapper)pipe).tile.pipe;
				return supplier.isRequestFailed();
			}
			if (((PipeWrapper)pipe).tile.pipe instanceof PipeItemsFluidSupplier) {
				PipeItemsFluidSupplier supplier = (PipeItemsFluidSupplier) ((PipeWrapper)pipe).tile.pipe;
				return supplier.isRequestFailed();
			}
		}
		return false;
	}

	@Override
	public boolean requiresParameter() {
		return false;
	}

}
