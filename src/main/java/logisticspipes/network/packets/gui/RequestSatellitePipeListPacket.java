package logisticspipes.network.packets.gui;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.BooleanCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.ExitRoute;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.tuples.Pair;

@StaticResolve
public class RequestSatellitePipeListPacket extends BooleanCoordinatesPacket {

	public RequestSatellitePipeListPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld(), LTGPCompletionCheck.PIPE);
		if (pipe == null || !(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		CoreRoutedPipe rPipe = (CoreRoutedPipe) pipe.pipe;
		List<Pair<String, UUID>> list;
		if (rPipe.getRouter() == null || rPipe.getRouter().getRouteTable() == null) {
			return;
		}
		if (this.isFlag()) {
			list = PipeFluidSatellite.AllSatellites.stream()
					.filter(Objects::nonNull)
					.filter(it -> it.getRouter() != null)
					.filter(it -> {
						List<List<ExitRoute>> routingTable = rPipe.getRouter().getRouteTable();
						return routingTable.size() > it.getRouterId() && routingTable.get(it.getRouterId()) != null && !routingTable.get(it.getRouterId()).isEmpty();
					})
					.sorted(Comparator.comparingDouble(it -> rPipe.getRouter().getRouteTable().get(it.getRouterId()).stream().map(it1 -> it1.distanceToDestination).min(Double::compare).get()))
					.map(it -> new Pair<>(it.getSatellitePipeName(), it.getRouter().getId()))
					.collect(Collectors.toList());
		} else {
			list = PipeItemsSatelliteLogistics.AllSatellites.stream()
					.filter(Objects::nonNull)
					.filter(it -> it.getRouter() != null)
					.filter(it -> {
						List<List<ExitRoute>> routingTable = rPipe.getRouter().getRouteTable();
						return routingTable.size() > it.getRouterId() && routingTable.get(it.getRouterId()) != null && !routingTable.get(it.getRouterId()).isEmpty();
					})
					.sorted(Comparator.comparingDouble(it -> rPipe.getRouter().getRouteTable().get(it.getRouterId()).stream().map(it1 -> it1.distanceToDestination).min(Double::compare).get()))
					.map(it -> new Pair<>(it.getSatellitePipeName(), it.getRouter().getId()))
					.collect(Collectors.toList());
		}
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ProvideSatellitePipeListPacket.class).setList(list), player);
	}

	@Override
	public ModernPacket template() {
		return new RequestSatellitePipeListPacket(getId());
	}
}
