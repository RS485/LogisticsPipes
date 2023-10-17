package logisticspipes.network.packets.satpipe;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.StringCoordinatesPacket;
import logisticspipes.pipes.SatelliteNamingResult;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.SatellitePipe;

@StaticResolve
public class SatelliteSetNamePacket extends StringCoordinatesPacket {

	public SatelliteSetNamePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld(), LTGPCompletionCheck.PIPE);
		if (pipe == null || pipe.pipe == null) {
			return;
		}
		String newName = getString();
		SatelliteNamingResult result = null;
		if (newName.trim().isEmpty()) {
			result = SatelliteNamingResult.BLANK_NAME;
		} else if (pipe.pipe instanceof SatellitePipe) {
			final SatellitePipe satellitePipe = (SatellitePipe) pipe.pipe;
			if (satellitePipe.getSatellitesOfType().stream().anyMatch(it -> it.getSatellitePipeName().equals(newName))) {
				result = SatelliteNamingResult.DUPLICATE_NAME;
			} else {
				result = SatelliteNamingResult.SUCCESS;
				satellitePipe.setSatellitePipeName(newName);
				satellitePipe.updateWatchers();
				satellitePipe.ensureAllSatelliteStatus();
			}
		}
		if (result != null) {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SetNameResult.class).setResult(result).setNewName(getString()), player);
		}
	}

	@Override
	public ModernPacket template() {
		return new SatelliteSetNamePacket(getId());
	}
}
