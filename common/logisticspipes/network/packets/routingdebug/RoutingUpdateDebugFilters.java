package logisticspipes.network.packets.routingdebug;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import logisticspipes.interfaces.routing.IFilter;
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
public class RoutingUpdateDebugFilters extends ModernPacket {

	@Getter
	@Setter
	private LPPosition pos;

	@Setter
	private EnumMap<PipeRoutingConnectionType, List<List<IFilter>>> filters;

	@Getter
	private EnumMap<PipeRoutingConnectionType, List<List<LPPosition>>> filterPositions;

	public RoutingUpdateDebugFilters(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		pos = data.readLPPosition();
		filterPositions = new EnumMap<PipeRoutingConnectionType, List<List<LPPosition>>>(PipeRoutingConnectionType.class);
		short id;
		while ((id = data.readShort()) != -1) {
			PipeRoutingConnectionType type = PipeRoutingConnectionType.values[id];
			List<List<LPPosition>> typeFilters = new ArrayList<List<LPPosition>>();
			int length;
			while ((length = data.readShort()) != -1) {
				List<LPPosition> linkedFilter = new ArrayList<LPPosition>();
				for (int i = 0; i < length; i++) {
					linkedFilter.add(data.readLPPosition());
				}
				typeFilters.add(linkedFilter);
			}
			filterPositions.put(type, typeFilters);
		}
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ClientViewController.instance().handlePacket(this);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeLPPosition(pos);
		for (PipeRoutingConnectionType type : filters.keySet()) {
			data.writeShort(type.ordinal());
			for (List<IFilter> linkedFilter : filters.get(type)) {
				data.writeShort(linkedFilter.size());
				for (IFilter filter : linkedFilter) {
					data.writeLPPosition(filter.getLPPosition());
				}
			}
			data.writeShort(-1);
		}
		data.writeShort(-1);
	}

	@Override
	public ModernPacket template() {
		return new RoutingUpdateDebugFilters(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
