package logisticspipes.network.packets.routingdebug;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.debug.ClientViewController;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class RoutingUpdateCanidatePipe extends ModernPacket {

	@Getter
	@Setter
	private ExitRoute exitRoute;

	public RoutingUpdateCanidatePipe(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ClientViewController.instance().handlePacket(this);
	}

	@Override
	public ModernPacket template() {
		return new RoutingUpdateCanidatePipe(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		exitRoute = data.readExitRoute(MainProxy.getClientMainWorld());
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeExitRoute(exitRoute);
	}
}
