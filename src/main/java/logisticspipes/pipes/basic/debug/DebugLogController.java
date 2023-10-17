package logisticspipes.pipes.basic.debug;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.debug.SendNewLogLine;
import logisticspipes.network.packets.debug.SendNewLogWindow;
import logisticspipes.network.packets.debug.UpdateStatusEntries;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.PlayerCollectionList;

public class DebugLogController {

	private static int nextID = 0;
	private final int ID = DebugLogController.nextID++;
	public final CoreUnroutedPipe pipe;
	public boolean debugThisPipe = false;
	private List<StatusEntry> oldList = new ArrayList<>();
	private PlayerCollectionList players = new PlayerCollectionList();

	public DebugLogController(CoreUnroutedPipe pipe) {
		this.pipe = pipe;
	}

	public void log(String info) {
		if (players.isEmptyWithoutCheck()) {
			return;
		}
		MainProxy.sendToPlayerList(PacketHandler.getPacket(SendNewLogLine.class).setWindowID(ID).setLine(info), players);
	}

	public void tick() {
		if (players.isEmpty()) {
			return;
		}
		generateStatus();
	}

	public void generateStatus() {
		List<StatusEntry> status = new ArrayList<>();
		pipe.addStatusInformation(status);
		if (!status.equals(oldList)) {
			MainProxy.sendToPlayerList(PacketHandler.getPacket(UpdateStatusEntries.class).setWindowID(ID).setStatus(status), players);
			oldList = status;
		}
	}

	public void openForPlayer(EntityPlayer player) {
		players.add(player);
		List<StatusEntry> status = new ArrayList<>();
		pipe.addStatusInformation(status);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SendNewLogWindow.class).setWindowID(ID).setTitle(pipe.toString()), player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(UpdateStatusEntries.class).setWindowID(ID).setStatus(status), player);
	}
}
