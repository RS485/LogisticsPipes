/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.main;

import java.util.LinkedList;

import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.PipeItemsSupplierLogistics;


import net.minecraft.src.Block;
import net.minecraft.src.TileEntity;
import buildcraft.api.transport.IPipe;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerProvider;

public class LogisticsTriggerProvider implements ITriggerProvider{

	@Override
	public LinkedList<ITrigger> getPipeTriggers(IPipe pipe) {
		if (!(pipe instanceof PipeItemsSupplierLogistics)) return null;
		LinkedList<ITrigger> triggers = new LinkedList<ITrigger>();
		triggers.add(LogisticsPipes.LogisticsFailedTrigger);
		return triggers;
	}

	@Override
	public LinkedList<ITrigger> getNeighborTriggers(Block block, TileEntity tile) {
		return null;
	}
}
