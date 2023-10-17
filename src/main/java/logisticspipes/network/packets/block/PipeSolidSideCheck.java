package logisticspipes.network.packets.block;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class PipeSolidSideCheck extends IntegerCoordinatesPacket {

	public PipeSolidSideCheck(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new PipeSolidSideCheck(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.world, LTGPCompletionCheck.PIPE);
		pipe.renderState.checkForRenderUpdate(player.world, pipe.getPos());
	}
}
