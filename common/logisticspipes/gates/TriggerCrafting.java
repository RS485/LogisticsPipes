package logisticspipes.gates;

import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.textures.provider.LPActionTriggerIconProvider;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.Pipe;

public class TriggerCrafting extends LPTrigger implements ITriggerPipe {

	public TriggerCrafting(int id) {
		super(id,"LogisticsPipes.triggers.isCrafting");
	}

	@Override
	public boolean isTriggerActive(Pipe pipe, ITriggerParameter parameter) {
		if (!(pipe instanceof PipeItemsCraftingLogistics)) return false;
		return ((PipeItemsCraftingLogistics)pipe).waitingForCraft;
	}

	@Override
	public int getIconIndex() {
		return LPActionTriggerIconProvider.triggerCraftingIconIndex;
	}

	@Override
	public String getDescription() {
		return "Pipe Waiting for Crafting";
	}

}
