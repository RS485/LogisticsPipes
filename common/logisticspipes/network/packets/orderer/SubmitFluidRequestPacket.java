package logisticspipes.network.packets.orderer;

import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.RequestPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestHandler;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.entity.player.EntityPlayer;

public class SubmitFluidRequestPacket extends RequestPacket {

	public SubmitFluidRequestPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SubmitFluidRequestPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = MainProxy.proxy.getPipeInDimensionAt(getDimension(), getPosX(), getPosY(), getPosZ(), player);
		if (pipe == null) {
			return;
		}
		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		if (!(pipe.pipe instanceof IRequestFluid)) {
			return;
		}
		
		RequestHandler.requestFluid(player, getStacks(), (CoreRoutedPipe) pipe.pipe, (IRequestFluid) pipe.pipe);
	}
}

