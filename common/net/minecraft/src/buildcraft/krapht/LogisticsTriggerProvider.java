/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht;

import java.util.LinkedList;

import net.minecraft.src.Block;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.api.IPipe;
import net.minecraft.src.buildcraft.api.ITriggerProvider;
import net.minecraft.src.buildcraft.api.Trigger;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsSupplierLogistics;

public class LogisticsTriggerProvider implements ITriggerProvider{

	@Override
	public LinkedList<Trigger> getPipeTriggers(IPipe pipe) {
		if (!(pipe instanceof PipeItemsSupplierLogistics)) return null;
		LinkedList<Trigger> triggers = new LinkedList<Trigger>();
		triggers.add(mod_LogisticsPipes.LogisticsFailedTrigger);
		return triggers;
	}

	@Override
	public LinkedList<Trigger> getNeighborTriggers(Block block, TileEntity tile) {
		return null;
	}
}
