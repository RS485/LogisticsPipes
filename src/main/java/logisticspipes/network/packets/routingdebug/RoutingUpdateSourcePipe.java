package logisticspipes.network.packets.routingdebug;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.debug.ClientViewController;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class RoutingUpdateSourcePipe extends ModernPacket {

	@Getter
	@Setter
	private ExitRoute exitRoute;

	public RoutingUpdateSourcePipe(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if (exitRoute != null) {
			ClientViewController.instance().handlePacket(this);
		}
	}

	@Override
	public ModernPacket template() {
		return new RoutingUpdateSourcePipe(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}

	@Override
	public void readData(LPDataInput input) {
		try {
			exitRoute = new ExitRoute(input);
		} catch (RuntimeException e) {
			LogisticsPipes.log.error("Could not read ExitRoute from RoutingUpdateSourcePipe", e);
		}
	}

	@Override
	public void writeData(LPDataOutput output) {
		exitRoute.write(output);
	}
}
