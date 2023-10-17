package logisticspipes.network.packets.routingdebug;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.debug.ClientViewController;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.world.DoubleCoordinates;

@StaticResolve
public class RoutingUpdateDebugFilters extends ModernPacket {

	@Getter
	@Setter
	private DoubleCoordinates pos;

	@Setter
	private EnumMap<PipeRoutingConnectionType, List<List<IFilter>>> filters;

	@Getter
	private EnumMap<PipeRoutingConnectionType, List<List<DoubleCoordinates>>> filterPositions;

	public RoutingUpdateDebugFilters(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		pos = new DoubleCoordinates(input);
		filterPositions = new EnumMap<>(PipeRoutingConnectionType.class);
		short id;
		while ((id = input.readShort()) != -1) {
			PipeRoutingConnectionType type = PipeRoutingConnectionType.values[id];
			List<List<DoubleCoordinates>> typeFilters = new ArrayList<>();
			int length;
			while ((length = input.readShort()) != -1) {
				List<DoubleCoordinates> linkedFilter = new ArrayList<>();
				for (int i = 0; i < length; i++) {
					linkedFilter.add(new DoubleCoordinates(input));
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
	public void writeData(LPDataOutput output) {
		output.writeSerializable(pos);
		for (PipeRoutingConnectionType type : filters.keySet()) {
			output.writeShort(type.ordinal());
			for (List<IFilter> linkedFilter : filters.get(type)) {
				output.writeShort(linkedFilter.size());
				for (IFilter filter : linkedFilter) {
					output.writeSerializable(filter.getLPPosition());
				}
			}
			output.writeShort(-1);
		}
		output.writeShort(-1);
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
