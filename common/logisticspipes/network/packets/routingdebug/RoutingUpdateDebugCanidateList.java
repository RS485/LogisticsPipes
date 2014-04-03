package logisticspipes.network.packets.routingdebug;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.debug.ClientViewController;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;

@Accessors(chain=true)
public class RoutingUpdateDebugCanidateList extends ModernPacket {
	
	@Getter
	@Setter
	private ExitRoute[] msg;
	
	public RoutingUpdateDebugCanidateList(int id) {
		super(id);
	}
	
	@Override
	public void readData(LPDataInputStream data) throws IOException {
		msg = new ExitRoute[data.readInt()];
		for(int i=0;i<msg.length;i++) {
			msg[i] = data.readExitRoute(MainProxy.getClientMainWorld());
		}
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		ClientViewController.instance().updateList(this);
	}
	
	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeInt(msg.length);
		for(int i=0;i<msg.length;i++) {
			data.writeExitRoute(msg[i]);
		}
	}
	
	@Override
	public ModernPacket template() {
		return new RoutingUpdateDebugCanidateList(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}

