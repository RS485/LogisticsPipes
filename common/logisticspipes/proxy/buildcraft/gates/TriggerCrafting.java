package logisticspipes.proxy.buildcraft.gates;

import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.proxy.buildcraft.subproxies.LPBCPipe;
import logisticspipes.textures.provider.LPActionTriggerIconProvider;

import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.transport.Pipe;

public class TriggerCrafting extends LPTrigger implements ITriggerInternal {

	public TriggerCrafting() {
		super("LogisticsPipes:trigger.isCrafting");
	}

	@Override
	public boolean isTriggerActive(Pipe pipe, IStatementParameter parameter) {
		if (pipe instanceof LPBCPipe) {
			if (!(((LPBCPipe) pipe).pipe.pipe instanceof PipeItemsCraftingLogistics)) {
				return false;
			}
			return ((PipeItemsCraftingLogistics) ((LPBCPipe) pipe).pipe.pipe).getLogisticsModule().waitingForCraft;
		}
		return false;
	}

	@Override
	public int getIconIndex() {
		return LPActionTriggerIconProvider.triggerCraftingIconIndex;
	}

	@Override
	public String getDescription() {
		return "Pipe Waiting for Crafting";
	}

	@Override
	public boolean requiresParameter() {
		return false;
	}
}
