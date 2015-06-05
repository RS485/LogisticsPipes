package logisticspipes.network.packets.pipe;

import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;

import net.minecraft.entity.player.EntityPlayer;

import lombok.experimental.Accessors;

@Accessors(chain = true)
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
		if (MainProxy.isClient(player.worldObj)) {
			final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
			if (pipe == null) {
				return;
			}
			if (pipe.pipe instanceof PipeItemsInvSysConnector) {
				PipeItemsInvSysConnector invCon = (PipeItemsInvSysConnector) pipe.pipe;
				invCon.resistance = getInteger();
			}
		} else {
			final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
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
