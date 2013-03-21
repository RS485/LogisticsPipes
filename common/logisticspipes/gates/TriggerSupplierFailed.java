/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gates;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import logisticspipes.pipes.PipeItemsBuilderSupplierLogistics;
import logisticspipes.pipes.PipeItemsLiquidSupplier;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.textures.Textures;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.ITrigger;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.TileGenericPipe;

import buildcraft.transport.Pipe;

public class TriggerSupplierFailed implements ITrigger{

	int id;
	public TriggerSupplierFailed(int id) {
		this.id = id;
	}
	
	@Override
	public String getDescription() {
		return "Supplier failed";
	}

	@Override
	public boolean isTriggerActive(TileEntity tile, ITriggerParameter parameter) {
		if(!(tile instanceof TileGenericPipe))
			return false;
		Pipe pipe = ((TileGenericPipe)tile).pipe;
			
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

	@Override
	public Icon getTextureIcon()  {
		return Textures.LOGISTICSACTIONTRIGGERS_SUPPLIER_FAILED_ICON;
	}

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasParameter() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ITriggerParameter createParameter() {
		// TODO Auto-generated method stub
		return null;
	}
}
