package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class InvSysConResistance extends IntegerCoordinatesPacket {

	public InvSysConResistance(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new InvSysConResistance(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if (MainProxy.isClient(player.world)) {
			final LogisticsTileGenericPipe pipe = this.getPipe(player.world);
			if (pipe == null) {
				return;
			}
			if (pipe.pipe instanceof PipeItemsInvSysConnector) {
				PipeItemsInvSysConnector invCon = (PipeItemsInvSysConnector) pipe.pipe;
				invCon.resistance = getInteger();
			}
		} else {
			final LogisticsTileGenericPipe pipe = this.getPipe(player.world);
			if (pipe == null) {
				return;
			}
			if (pipe.pipe instanceof PipeItemsInvSysConnector) {
				PipeItemsInvSysConnector invCon = (PipeItemsInvSysConnector) pipe.pipe;
				invCon.resistance = getInteger();
				invCon.getRouter().update(true, invCon);
			}
		}
	}
}
