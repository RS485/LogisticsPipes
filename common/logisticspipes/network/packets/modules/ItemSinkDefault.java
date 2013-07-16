package logisticspipes.network.packets.modules;

import logisticspipes.modules.ModuleItemSink;
import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

public class ItemSinkDefault extends Integer2CoordinatesPacket {

	public ItemSinkDefault(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ItemSinkDefault(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if(getInteger2() == -1) {
			if (!(pipe.pipe instanceof CoreRoutedPipe)) {
				return;
			}
			if(!(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleItemSink)) {
				return;
			}
			ModuleItemSink module = (ModuleItemSink) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule();
			module.setDefaultRoute(getInteger() == 1);
			return;
		}
		if (!(pipe.pipe instanceof PipeLogisticsChassi)) {
			return;
		}
		if(((PipeLogisticsChassi)pipe.pipe).getModules() == null) return;
		if(((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getInteger2()) instanceof ModuleItemSink) {
			ModuleItemSink module = (ModuleItemSink) ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getInteger2());
			module.setDefaultRoute(getInteger() == 1);
		}
	}
}

