package logisticspipes.proxy.buildcraft.gates;

import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.textures.provider.LPActionTriggerIconProvider;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.transport.IPipeTrigger;
import buildcraft.transport.Pipe;

public class TriggerCrafting extends LPTrigger implements IPipeTrigger {

	public TriggerCrafting() {
		super("LogisticsPipes:trigger.isCrafting");
	}

	@Override
	public boolean isTriggerActive(Pipe pipe, ITriggerParameter parameter) {
		if (!(pipe instanceof PipeItemsCraftingLogistics)) return false;
		return ((PipeItemsCraftingLogistics)pipe).getLogisticsModule().waitingForCraft;
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
