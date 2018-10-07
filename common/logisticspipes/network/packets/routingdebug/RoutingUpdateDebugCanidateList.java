package logisticspipes.network.packets.routingdebug;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.debug.ClientViewController;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

import logisticspipes.utils.StaticResolve;

@StaticResolve
public class RoutingUpdateDebugCanidateList extends ModernPacket {

	@Getter
	@Setter
	private ExitRoute[] msg;

	public RoutingUpdateDebugCanidateList(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		msg = new ExitRoute[input.readInt()];
		for (int i = 0; i < msg.length; i++) {
			msg[i] = new ExitRoute(input);
		}
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ClientViewController.instance().updateList(this);
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeInt(msg.length);
		for (ExitRoute element : msg) {
			element.write(output);
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
