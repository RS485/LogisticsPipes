package logisticspipes.network.packets.modules;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.entity.player.EntityPlayer;

public class CrafterDefault extends Integer2CoordinatesPacket {

	public CrafterDefault(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CrafterDefault(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if (getInteger2() == -1) {
			if (!(pipe.pipe instanceof CoreRoutedPipe)) {
				return;
			}
			if (!(((CoreRoutedPipe) pipe.pipe).getLogisticsModule() instanceof ModuleCrafter)) {
				return;
			}
			ModuleCrafter module = (ModuleCrafter) ((CoreRoutedPipe) pipe.pipe).getLogisticsModule();
			//module.setDefaultRoute(getInteger() == 1);
			return;
		}
		if (!(pipe.pipe instanceof PipeLogisticsChassi)) {
			return;
		}
		if (((PipeLogisticsChassi) pipe.pipe).getModules() == null) {
			return;
		}
		if (((PipeLogisticsChassi) pipe.pipe).getModules().getSubModule(getInteger2()) instanceof ModuleCrafter) {
			ModuleCrafter module = (ModuleCrafter) ((PipeLogisticsChassi) pipe.pipe).getModules().getSubModule(getInteger2());
			//module.setDefaultRoute(getInteger() == 1);
		}
	}
}
