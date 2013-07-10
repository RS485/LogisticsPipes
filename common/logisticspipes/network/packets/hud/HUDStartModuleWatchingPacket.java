package logisticspipes.network.packets.hud;

import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

public class HUDStartModuleWatchingPacket extends IntegerCoordinatesPacket {

	public HUDStartModuleWatchingPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new HUDStartModuleWatchingPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getInteger()) instanceof IModuleWatchReciver) {
			IModuleWatchReciver handler = (IModuleWatchReciver) ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getInteger());
			handler.startWatching(player);
		}
	}
}

