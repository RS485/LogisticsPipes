package logisticspipes.network.packets.modules;

import logisticspipes.gui.modules.GuiCCBasedQuickSort;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleCCBasedQuickSort;
import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyModuleContainer;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.client.FMLClientHandler;

public class CCBasedQuickSortMode extends Integer2CoordinatesPacket {
	
	public CCBasedQuickSortMode(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if(getInteger2() < 0) {
			if(MainProxy.isClient(player.worldObj)) {
				if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiCCBasedQuickSort) {
					((GuiCCBasedQuickSort) FMLClientHandler.instance().getClient().currentScreen).setTimeOut(getInteger());
				}
			} else {
				if(player.openContainer instanceof DummyModuleContainer) {
					DummyModuleContainer dummy = (DummyModuleContainer) player.openContainer;
					if(dummy.getModule() instanceof ModuleCCBasedQuickSort) {
						final ModuleCCBasedQuickSort module = (ModuleCCBasedQuickSort) dummy.getModule();
						module.setTimeout(getInteger());
					}
				}
			}
			return;
		}
		LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) return;
		if(pipe.pipe instanceof PipeLogisticsChassi) {
			LogisticsModule module = ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getInteger2());
			if(module instanceof ModuleCCBasedQuickSort) {
				((ModuleCCBasedQuickSort)module).setTimeout(getInteger());
			}
		}
	}
	
	@Override
	public ModernPacket template() {
		return new CCBasedQuickSortMode(getId());
	}
}
