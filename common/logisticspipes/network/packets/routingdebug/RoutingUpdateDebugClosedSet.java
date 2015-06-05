package logisticspipes.network.packets.routingdebug;

import java.io.IOException;
import java.util.EnumSet;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.debug.ClientViewController;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class RoutingUpdateDebugClosedSet extends ModernPacket {

	@Getter
	@Setter
	private LPPosition pos;

	@Getter
	@Setter
	private EnumSet<PipeRoutingConnectionType> set;

	public RoutingUpdateDebugClosedSet(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		set = data.readEnumSet(PipeRoutingConnectionType.class);
		pos = data.readLPPosition();
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ClientViewController.instance().handlePacket(this);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeEnumSet(set, PipeRoutingConnectionType.class);
		data.writeLPPosition(pos);
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
