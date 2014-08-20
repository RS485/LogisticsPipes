/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.proxy.buildcraft.gates;

import java.util.LinkedList;

import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import logisticspipes.proxy.buildcraft.gates.wrapperclasses.TilePipeWrapper;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerProvider;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.Gate;
import buildcraft.transport.gates.GateDefinition;

public class LogisticsTriggerProvider implements ITriggerProvider {

	@Override
	public LinkedList<ITrigger> getPipeTriggers(IPipeTile pipe) {
		if(pipe instanceof TilePipeWrapper) {
			LogisticsTileGenericPipe lPipe = ((TilePipeWrapper)pipe).tile;
			LinkedList<ITrigger> triggers = new LinkedList<ITrigger>();
			if (lPipe.pipe instanceof PipeItemsSupplierLogistics || lPipe.pipe instanceof PipeItemsFluidSupplier) {
				triggers.add(BuildCraftProxy.LogisticsFailedTrigger);
			}
			if(lPipe.pipe instanceof PipeItemsCraftingLogistics) {
				triggers.add(BuildCraftProxy.LogisticsCraftingTrigger);
			}
	        if (lPipe.pipe instanceof CoreRoutedPipe) {
	            //Only show this conditional on Gates that can accept parameters
	            if (((CoreRoutedPipe) lPipe.pipe).hasGate()) {
	                Gate gate = (Gate) lPipe.pipe.bcPipePart.getGate();
	                if ((gate.logic == GateDefinition.GateLogic.AND || gate.logic == GateDefinition.GateLogic.OR) && gate.material == GateDefinition.GateMaterial.DIAMOND) {
	                    triggers.add(BuildCraftProxy.LogisticsHasDestinationTrigger);
	                }
	            }
	        }
	        if(!triggers.isEmpty()) {
	        	return triggers;
	        }
		}
        return null;
	}
	
	@Override
	public LinkedList<ITrigger> getNeighborTriggers(Block block, TileEntity tile) {
		if(tile instanceof LogisticsPowerJunctionTileEntity){
			LinkedList<ITrigger> triggers = new  LinkedList<ITrigger>();
			triggers.add(BuildCraftProxy.LogisticsNeedPowerTrigger);
			return triggers;
		}
		if(tile instanceof LogisticsSolderingTileEntity){
			LinkedList<ITrigger> triggers = new  LinkedList<ITrigger>();
			triggers.add(BuildCraftProxy.LogisticsNeedPowerTrigger);
			return triggers;
		}
		return null;
	}
}
