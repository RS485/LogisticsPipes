package logisticspipes.network.packets.routingdebug;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.debug.ClientViewController;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.world.DoubleCoordinates;

@StaticResolve
public class RoutingUpdateDebugClosedSet extends ModernPacket {

	@Getter
	@Setter
	private DoubleCoordinates pos;

	@Getter
	@Setter
	private EnumSet<PipeRoutingConnectionType> set;

	public RoutingUpdateDebugClosedSet(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		set = input.readEnumSet(PipeRoutingConnectionType.class);
		pos = new DoubleCoordinates(input);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ClientViewController.instance().handlePacket(this);
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeEnumSet(set, PipeRoutingConnectionType.class);
		output.writeSerializable(pos);
	}

	@Override
	public ModernPacket template() {
		return new RoutingUpdateDebugClosedSet(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
