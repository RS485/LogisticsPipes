/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.main;

import java.util.HashMap;
import java.util.LinkedList;

import logisticspipes.LogisticsPipes;
import logisticspipes.logic.BaseRoutingLogic;


import buildcraft.BuildCraftTransport;
import net.minecraft.src.NBTTagCompound;
//import net.minecraft.src.buildcraft.core.Action;
import buildcraft.api.gates.Action;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionReceptor;
import buildcraft.api.gates.Trigger;
import buildcraft.api.gates.TriggerParameter;
import buildcraft.core.ActionRedstoneOutput;
import buildcraft.transport.ActionSignalOutput;
import buildcraft.transport.Pipe;
//import net.minecraft.src.buildcraft.transport.Pipe.GateKind;

public abstract class RoutedPipe extends CoreRoutedPipe {
	
	public RoutedPipe(BaseRoutingLogic logic, int itemID) {
		super(logic, itemID);
	}
	
	@Override
	public void onNeighborBlockChange(int blockId) {
		super.onNeighborBlockChange(blockId);
		onNeighborBlockChange_Logistics();
	}

	@Override
	public LinkedList<IAction> getActions() {
		LinkedList<IAction> actions = super.getActions();
		actions.add(LogisticsPipes.LogisticsDisableAction);
		return actions;
	}
	
	@Override
	protected void actionsActivated(HashMap<Integer, Boolean> actions) {
		super.actionsActivated(actions);

		setEnabled(true);
		// Activate the actions
		for (Integer i : actions.keySet()) {
			if (actions.get(i)) {
				if (ActionManager.actions[i] instanceof ActionDisableLogistics){
					setEnabled(false);
				}
			}
		}
	}
}
