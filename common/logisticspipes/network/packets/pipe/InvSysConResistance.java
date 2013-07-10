package logisticspipes.network.packets.pipe;

import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.proxy.MainProxy;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
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
		if(MainProxy.isClient(player.worldObj)) {
			final TileGenericPipe pipe = this.getTile(player.worldObj, TileGenericPipe.class);
			if(pipe == null) {
				return;
			}
			if(pipe.pipe instanceof PipeItemsInvSysConnector) {
				PipeItemsInvSysConnector invCon = (PipeItemsInvSysConnector) pipe.pipe;
				invCon.resistance = getInteger();
			}
		} else {
			final TileGenericPipe pipe = this.getTile(player.worldObj, TileGenericPipe.class);
			if(pipe == null) {
				return;
			}
			if(pipe.pipe instanceof PipeItemsInvSysConnector) {
				PipeItemsInvSysConnector invCon = (PipeItemsInvSysConnector) pipe.pipe;
				invCon.resistance = getInteger();
				invCon.getRouter().update(true);
			}
		}
	}
}

