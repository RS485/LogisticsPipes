/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes.basic;

import java.util.HashMap;
import java.util.LinkedList;

import logisticspipes.logic.BaseRoutingLogic;
import logisticspipes.main.ActionDisableLogistics;
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import logisticspipes.transport.PipeTransportLogistics;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;

public abstract class RoutedPipe extends CoreRoutedPipe {
	
	public RoutedPipe(BaseRoutingLogic logic, int itemID) {
		super(logic, itemID);
	}
	
	public RoutedPipe(PipeTransportLogistics transport, BaseRoutingLogic logic, int itemID) {
		super(transport, logic, itemID);
	}

	@Override
	public LinkedList<IAction> getActions() {
		LinkedList<IAction> actions = super.getActions();
		actions.add(BuildCraftProxy.LogisticsDisableAction);
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
