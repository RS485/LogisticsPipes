package logisticspipes.network.packets.cpipe;

import logisticspipes.network.abstractpackets.InventoryCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain = true)
public class CPipeSatelliteImportBack extends InventoryCoordinatesPacket {
	
	public CPipeSatelliteImportBack(int id) {
		super(id);
	}
	
	@Override
	public ModernPacket template() {
		return new CPipeSatelliteImportBack(getId());
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		
		if( !(pipe.pipe instanceof PipeItemsCraftingLogistics)) {
			return;
		}
		
		final PipeItemsCraftingLogistics craftingPipe = (PipeItemsCraftingLogistics) pipe.pipe;
		for(int i = 0; i < getStackList().size(); i++) {
			craftingPipe.setDummyInventorySlot(i, getStackList().get(i));
		}
	}
}

