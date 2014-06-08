package logisticspipes.network.packets.cpipe;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;

public class CPipePrevSatellite extends CoordinatesPacket {
	
	public CPipePrevSatellite(int id) {
		super(id);
	}
	
	@Override
	public ModernPacket template() {
		return new CPipePrevSatellite(getId());
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		
		if( !(pipe.pipe instanceof PipeItemsCraftingLogistics)) {
			return;
		}
		
		((PipeItemsCraftingLogistics) pipe.pipe).getLogisticsModule().setPrevSatellite(player);
	}
	
}

