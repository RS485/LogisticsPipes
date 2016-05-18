package logisticspipes.network.packets.routingdebug;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.debug.ClientViewController;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

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
	public void readData(LPDataInput input) {
		exitRoute = input.readExitRoute(MainProxy.getClientMainWorld());
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeExitRoute(exitRoute);
	}
}
