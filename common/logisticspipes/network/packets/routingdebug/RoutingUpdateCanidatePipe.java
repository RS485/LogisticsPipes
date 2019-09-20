package logisticspipes.network.packets.routingdebug;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.debug.ClientViewController;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class RoutingUpdateCandidatePipe extends ModernPacket {

	@Getter
	@Setter
	private ExitRoute exitRoute;

	public RoutingUpdateCandidatePipe(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ClientViewController.instance().handlePacket(this);
	}

	@Override
	public ModernPacket template() {
		return new RoutingUpdateCandidatePipe(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}

	@Override
	public void readData(LPDataInput input) {
		exitRoute = new ExitRoute(input);
	}

	@Override
	public void writeData(LPDataOutput output) {
		exitRoute.write(output);
	}
}
