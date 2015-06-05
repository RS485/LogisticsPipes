package logisticspipes.proxy.buildcraft.gates;

import logisticspipes.textures.provider.LPActionTriggerIconProvider;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;

public class ActionRobotRoutingLogistics extends LPAction {

	public ActionRobotRoutingLogistics() {
		super("LogisticsPipes:action.robotRouting");
	}

	@Override
	public String getDescription() {
		return "Activate Robot Routing";
	}

	@Override
	public int getIconIndex() {
		return LPActionTriggerIconProvider.actionRobotRoutingIconIndex;
	}

	@Override
	public void actionActivate(IStatementContainer paramIStatementContainer, IStatementParameter[] paramArrayOfIStatementParameter) {}
}
