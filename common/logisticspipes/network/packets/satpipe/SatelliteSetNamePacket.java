package logisticspipes.network.packets.satpipe;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.StringCoordinatesPacket;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.SatelliteNamingResult;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class SatelliteSetNamePacket extends StringCoordinatesPacket {

	public SatelliteSetNamePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld(), LTGPCompletionCheck.PIPE);
		if (pipe == null) {
			return;
		}
		String newName = getString();
		SatelliteNamingResult result;
		if (pipe.pipe instanceof PipeItemsSatelliteLogistics) {
			if (newName.trim().isEmpty()) {
				result = SatelliteNamingResult.BLANK_NAME;
			} else if (PipeItemsSatelliteLogistics.AllSatellites.stream().anyMatch(it -> it.satellitePipeName.equals(newName))) {
				result = SatelliteNamingResult.DUPLICATE_NAME;
			} else {
				result = SatelliteNamingResult.SUCCESS;
				((PipeItemsSatelliteLogistics) pipe.pipe).satellitePipeName = newName;
			}
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SetNameResult.class).setResult(result).setNewName(getString()), player);
		} else if (pipe.pipe instanceof PipeFluidSatellite) {
			if (newName.trim().isEmpty()) {
				result = SatelliteNamingResult.BLANK_NAME;
			} else if (PipeFluidSatellite.AllSatellites.stream().anyMatch(it -> it.satellitePipeName.equals(newName))) {
				result = SatelliteNamingResult.DUPLICATE_NAME;
			} else {
				result = SatelliteNamingResult.SUCCESS;
				((PipeFluidSatellite) pipe.pipe).satellitePipeName = newName;
			}
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SetNameResult.class).setResult(result).setNewName(getString()), player);
		}
	}

	@Override
	public ModernPacket template() {
		return new SatelliteSetNamePacket(getId());
	}
}
