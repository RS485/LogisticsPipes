package logisticspipes.network.packets.modules;

import logisticspipes.gui.modules.GuiExtractor;
import logisticspipes.interfaces.ISneakyDirectionReceiver;
import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.client.FMLClientHandler;

public class ExtractorModuleMode extends Integer2CoordinatesPacket {

	public ExtractorModuleMode(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ExtractorModuleMode(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if(getInteger2() < 0) {
			if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiExtractor) {
				((GuiExtractor) FMLClientHandler.instance().getClient().currentScreen).setMode(ForgeDirection.getOrientation(getInteger()));
			}
			return;
		}
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if(getInteger2() == -1) {
			if(pipe.pipe instanceof CoreRoutedPipe && ((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ISneakyDirectionReceiver) {
				((ISneakyDirectionReceiver)((CoreRoutedPipe)pipe.pipe).getLogisticsModule()).setSneakyDirection(ForgeDirection.getOrientation(getInteger()));
			}
			return;
		}
		if(pipe.pipe instanceof PipeLogisticsChassi && ((PipeLogisticsChassi)pipe.pipe).getModules() != null && ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getInteger2()) instanceof ISneakyDirectionReceiver) {
			ISneakyDirectionReceiver recieiver = (ISneakyDirectionReceiver) ((PipeLogisticsChassi)pipe.pipe).getModules().getSubModule(getInteger2());
			recieiver.setSneakyDirection(ForgeDirection.getOrientation(getInteger()));
		}
	}
}

