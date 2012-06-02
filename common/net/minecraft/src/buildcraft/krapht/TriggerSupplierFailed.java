/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht;

import net.minecraft.src.TileEntity;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.buildcraft.api.Trigger;
import net.minecraft.src.buildcraft.api.TriggerParameter;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsSupplierLogistics;
import net.minecraft.src.buildcraft.transport.ITriggerPipe;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;

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
	public boolean isTriggerActive(Pipe pipe, TriggerParameter parameter) {
		if (!(pipe instanceof PipeItemsSupplierLogistics)) return false;
		PipeItemsSupplierLogistics supplier = (PipeItemsSupplierLogistics) pipe;
		return supplier.isRequestFailed();
	}

	@Override
	public String getTextureFile() {
		return core_LogisticsPipes.LOGISTICSACTIONTRIGGERS_TEXTURE_FILE;
	}
	
	
}
