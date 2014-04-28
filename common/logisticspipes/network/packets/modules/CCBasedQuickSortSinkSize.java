package logisticspipes.network.packets.modules;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleCCBasedQuickSort;
import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;

public class CCBasedQuickSortSinkSize extends Integer2CoordinatesPacket {
	
	public CCBasedQuickSortSinkSize(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if(getInteger2() < 0) return;
		LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) return;
		if(pipe.pipe instanceof PipeLogisticsChassi) {
			LogisticsModule module = ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getInteger2());
			if(module instanceof ModuleCCBasedQuickSort) {
				((ModuleCCBasedQuickSort)module).setSinkSize(getInteger());
			}
		}
	}
	
	@Override
	public ModernPacket template() {
		return new CCBasedQuickSortSinkSize(getId());
	}
}
