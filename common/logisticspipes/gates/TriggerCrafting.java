package logisticspipes.gates;

import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.textures.Textures;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.Trigger;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.Pipe;

public class TriggerCrafting extends Trigger implements ITriggerPipe {

	public TriggerCrafting(int id) {
		super(id);
	}

	@Override
	public boolean isTriggerActive(Pipe pipe, ITriggerParameter parameter) {
		if (!(pipe instanceof PipeItemsCraftingLogistics)) return false;
		return ((PipeItemsCraftingLogistics)pipe).waitingForCraft;
	}

	@Override
	public int getIndexInTexture() {
		return 0 * 16 + 2;
	}

	@Override
	public String getDescription() {
		return "Pipe Waiting for Crafting";
	}

	@Override
	public String getTextureFile() {
		return Textures.LOGISTICSACTIONTRIGGERS_TEXTURE_FILE;
	}

}
