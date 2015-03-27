package logisticspipes.proxy.buildcraft.gates;

import logisticspipes.textures.provider.LPActionTriggerIconProvider;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
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
