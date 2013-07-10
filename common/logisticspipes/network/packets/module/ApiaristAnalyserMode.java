package logisticspipes.network.packets.module;

import logisticspipes.modules.ModuleApiaristAnalyser;
import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyModuleContainer;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class ApiaristAnalyserMode extends Integer2CoordinatesPacket {

	public ApiaristAnalyserMode(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ApiaristAnalyserMode(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if(MainProxy.isClient(player.worldObj)) {
			final TileGenericPipe pipe = this.getPipe(player.worldObj);
			if (pipe == null) return;
			if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getInteger2()) instanceof ModuleApiaristAnalyser) {
				((ModuleApiaristAnalyser)((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getInteger2())).setExtractMode(getInteger());
			}
			if(pipe.pipe instanceof CoreRoutedPipe && ((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleApiaristAnalyser) {
				((ModuleApiaristAnalyser)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).setExtractMode(getInteger());
			}
		} else {
			if(getInteger2() < 0) {
				if(player.openContainer instanceof DummyModuleContainer) {
					DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
					if(dummy.getModule() instanceof ModuleApiaristAnalyser) {
						ModuleApiaristAnalyser module = (ModuleApiaristAnalyser) dummy.getModule();
						module.setExtractMode(getInteger2());
					}
				}
				return;
			}
			final TileGenericPipe pipe = this.getPipe(player.worldObj);
			if(pipe == null) return;
			if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getInteger2()) instanceof ModuleApiaristAnalyser) {
				((ModuleApiaristAnalyser)((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getInteger2())).setExtractMode(getInteger());
			}
			if(pipe.pipe instanceof CoreRoutedPipe && ((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleApiaristAnalyser) {
				((ModuleApiaristAnalyser)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).setExtractMode(getInteger());
			}
		}
	}
}

