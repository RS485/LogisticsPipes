package logisticspipes.network.packets.modules;

import logisticspipes.gui.GuiSupplierPipe;
import logisticspipes.logic.PipeItemsSupplierLogistics;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.client.FMLClientHandler;

public class SupplierPipeMode extends IntegerCoordinatesPacket {

	public SupplierPipeMode(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SupplierPipeMode(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if (!(pipe.pipe.logic instanceof PipeItemsSupplierLogistics)) {
			return;
		}
		((PipeItemsSupplierLogistics) pipe.pipe.logic).setRequestingPartials(getInteger() == 1);
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiSupplierPipe) {
			((GuiSupplierPipe) FMLClientHandler.instance().getClient().currentScreen).refreshMode();
		}
	}
}

