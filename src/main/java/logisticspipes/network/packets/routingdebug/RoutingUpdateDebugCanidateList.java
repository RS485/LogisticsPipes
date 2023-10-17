package logisticspipes.network.packets.routingdebug;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
public class RoutingUpdateDebugCanidateList extends ModernPacket {

	@Getter
	@Setter
	private List<ExitRoute> exitRoutes;

	public RoutingUpdateDebugCanidateList(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		final LinkedList<ExitRoute> readExitRoutes = input.readLinkedList(objInput -> {
			try {
				return new ExitRoute(objInput);
			} catch (RuntimeException e) {
				LogisticsPipes.log.error("Could not read ExitRoute in RoutingUpdateDebugCanidateList", e);
			}
			return null;
		});
		if (readExitRoutes == null) {
			LogisticsPipes.log.error("Read a non-existent ExitRoute collection in RoutingUpdateDebugCanidateList");
			exitRoutes = Collections.emptyList();
			return;
		}
		exitRoutes = readExitRoutes.stream().filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ClientViewController.instance().updateList(this);
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeCollection(exitRoutes);
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
