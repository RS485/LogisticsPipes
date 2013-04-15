/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gates;

import logisticspipes.pipes.PipeItemsBuilderSupplierLogistics;
import logisticspipes.pipes.PipeItemsLiquidSupplier;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.textures.Textures;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.Pipe;

public class TriggerSupplierFailed extends BCTrigger implements ITriggerPipe{

	public TriggerSupplierFailed(int id) {
		super(id);
	}
	
	@Override
	public int getIconIndex() {
		return 0 * 16 + 0;
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
		if (pipe instanceof PipeItemsBuilderSupplierLogistics) {
			PipeItemsBuilderSupplierLogistics supplier = (PipeItemsBuilderSupplierLogistics) pipe;
			return supplier.isRequestFailed();
		}
		if (pipe instanceof PipeItemsLiquidSupplier) {
			PipeItemsLiquidSupplier supplier = (PipeItemsLiquidSupplier) pipe;
			return supplier.isRequestFailed();
		}
		return false;
	}

}
