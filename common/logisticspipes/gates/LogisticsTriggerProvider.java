/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gates;

import java.util.LinkedList;

import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity_BuildCraft;
import logisticspipes.pipes.PipeItemsBuilderSupplierLogistics;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsLiquidSupplier;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerProvider;
import buildcraft.api.transport.IPipe;

public class LogisticsTriggerProvider implements ITriggerProvider{

	@Override
	public LinkedList<ITrigger> getPipeTriggers(IPipe pipe) {
		if (pipe instanceof PipeItemsSupplierLogistics || pipe instanceof PipeItemsBuilderSupplierLogistics || pipe instanceof PipeItemsLiquidSupplier) {
			LinkedList<ITrigger> triggers = new LinkedList<ITrigger>();
			triggers.add(BuildCraftProxy.LogisticsFailedTrigger);
			return triggers;
		}
		if(pipe instanceof PipeItemsCraftingLogistics) {
			LinkedList<ITrigger> triggers = new LinkedList<ITrigger>();
			triggers.add(BuildCraftProxy.LogisticsCraftingTrigger);
			return triggers;
		}
		return null;
	}
	
	@Override
	public LinkedList<ITrigger> getNeighborTriggers(Block block, TileEntity tile) {
		if(tile instanceof LogisticsPowerJuntionTileEntity_BuildCraft){
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
