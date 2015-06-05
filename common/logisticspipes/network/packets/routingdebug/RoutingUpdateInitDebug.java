package logisticspipes.network.packets.routingdebug;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.routing.debug.ClientViewController;

import net.minecraft.entity.player.EntityPlayer;

public class RoutingUpdateInitDebug extends ModernPacket {

	public RoutingUpdateInitDebug(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {}

	@Override
	public void processPacket(EntityPlayer player) {
		ClientViewController.instance().init(this);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {}

	@Override
	public ModernPacket template() {
		return new RoutingUpdateInitDebug(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
