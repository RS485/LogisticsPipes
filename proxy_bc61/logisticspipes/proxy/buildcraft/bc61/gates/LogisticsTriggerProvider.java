/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.proxy.buildcraft.bc61.gates;

import java.util.Collection;
import java.util.LinkedList;

import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.buildcraft.bc61.BuildCraftProxy;
import logisticspipes.proxy.buildcraft.bc61.gates.wrapperclasses.TilePipeWrapper;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerProvider;
import buildcraft.api.transport.IPipeTile;

public class LogisticsTriggerProvider implements ITriggerProvider {

	@Override
	public Collection<ITriggerInternal> getInternalTriggers(IStatementContainer pipe) {
		if(pipe instanceof TilePipeWrapper) {
			LogisticsTileGenericPipe lPipe = ((TilePipeWrapper)pipe).tile;
			LinkedList<ITriggerInternal> triggers = new LinkedList<ITriggerInternal>();
			if (lPipe.pipe instanceof PipeItemsSupplierLogistics || lPipe.pipe instanceof PipeItemsFluidSupplier) {
				triggers.add(BuildCraftProxy.LogisticsFailedTrigger);
			}
			if(lPipe.pipe instanceof PipeItemsCraftingLogistics) {
				triggers.add(BuildCraftProxy.LogisticsCraftingTrigger);
			}
	        if (lPipe.pipe instanceof CoreRoutedPipe) {
	            //Only show this conditional on Gates that can accept parameters
	            triggers.add(BuildCraftProxy.LogisticsHasDestinationTrigger);
	        }
	        if(!triggers.isEmpty()) {
	        	return triggers;
	        }
		}
        return null;
	}
	
	@Override
	public Collection<ITriggerExternal> getExternalTriggers(ForgeDirection paramForgeDirection, TileEntity tile) {
		if(tile instanceof LogisticsPowerJunctionTileEntity){
			LinkedList<ITriggerExternal> triggers = new  LinkedList<ITriggerExternal>();
			triggers.add(BuildCraftProxy.LogisticsNeedPowerTrigger);
			return triggers;
		}
		if(tile instanceof LogisticsSolderingTileEntity){
			LinkedList<ITriggerExternal> triggers = new  LinkedList<ITriggerExternal>();
			triggers.add(BuildCraftProxy.LogisticsNeedPowerTrigger);
			return triggers;
		}
		return null;
	}
}
