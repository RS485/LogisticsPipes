package logisticspipes.network.packets.routingdebug;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.debug.ClientViewController;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class RoutingUpdateDebugCanidateList extends ModernPacket {

	@Getter
	@Setter
	private ExitRoute[] msg;

	public RoutingUpdateDebugCanidateList(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		msg = new ExitRoute[input.readInt()];
		for (int i = 0; i < msg.length; i++) {
			msg[i] = input.readExitRoute(MainProxy.getClientMainWorld());
		}
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ClientViewController.instance().updateList(this);
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		output.writeInt(msg.length);
		for (ExitRoute element : msg) {
			output.writeExitRoute(element);
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
