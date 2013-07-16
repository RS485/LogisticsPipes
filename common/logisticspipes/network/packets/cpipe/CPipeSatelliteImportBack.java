package logisticspipes.network.packets.cpipe;

import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.network.abstractpackets.InventoryCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
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
		
		if( !(pipe.pipe.logic instanceof BaseLogicCrafting)) {
			return;
		}
		
		final BaseLogicCrafting craftingPipe = (BaseLogicCrafting) pipe.pipe.logic;
		for(int i = 0; i < getStackList().size(); i++) {
			craftingPipe.setDummyInventorySlot(i, getStackList().get(i));
		}
	}
}

