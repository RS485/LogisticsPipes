package logisticspipes.network.packets.modules;

import logisticspipes.gui.modules.GuiAdvancedExtractor;
import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.client.FMLClientHandler;

public class AdvancedExtractorInclude extends Integer2CoordinatesPacket {

	public AdvancedExtractorInclude(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new AdvancedExtractorInclude(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if(getInteger2() < 0) {
			if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiAdvancedExtractor) {
				((GuiAdvancedExtractor) FMLClientHandler.instance().getClient().currentScreen).setInclude(getInteger() == 1);
			}
			return;
		}
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if(getInteger2() == -1) {
			if(pipe.pipe instanceof CoreRoutedPipe && ((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleAdvancedExtractor) {
				((ModuleAdvancedExtractor)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).setItemsIncluded(getInteger() == 1);
			}
			return;
		}
		if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getInteger2()) instanceof ModuleAdvancedExtractor) {
			ModuleAdvancedExtractor recieiver = (ModuleAdvancedExtractor) ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getInteger2());
			recieiver.setItemsIncluded(getInteger() == 1);
		}
	}
}

