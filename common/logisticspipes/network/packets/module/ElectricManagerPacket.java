package logisticspipes.network.packets.module;

import logisticspipes.modules.ModuleElectricManager;
import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyModuleContainer;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class ElectricManagerPacket extends Integer2CoordinatesPacket {

	public ElectricManagerPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ElectricManagerPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if(MainProxy.isClient(player.worldObj)) {
			final TileGenericPipe pipe = this.getPipe(player.worldObj);
			if(pipe == null) {
				return;
			}
			if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getInteger2()) instanceof ModuleElectricManager) {
				ModuleElectricManager module = (ModuleElectricManager) ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getInteger2());
				module.setDischargeMode(getInteger() == 1);
			}
		} else {
			if(getInteger2() < 0) {
				if(player.openContainer instanceof DummyModuleContainer) {
					DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
					if(dummy.getModule() instanceof ModuleElectricManager) {
						ModuleElectricManager module = (ModuleElectricManager) dummy.getModule();
						module.setDischargeMode(getInteger() == 1);
					}
				}
				return;
			}
			final TileGenericPipe pipe = this.getPipe(player.worldObj);
			if(pipe == null) {
				return;
			}
			if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getInteger2()) instanceof ModuleElectricManager) {
				ModuleElectricManager module = (ModuleElectricManager) ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getInteger2());
				module.setDischargeMode(getInteger() == 1);
			}
		}
	}
}

