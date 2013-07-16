package logisticspipes.network.packets.modules;

import logisticspipes.gui.GuiProviderPipe;
import logisticspipes.logic.LogicProvider;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.client.FMLClientHandler;

public class ProviderPipeInclude extends IntegerCoordinatesPacket {

	public ProviderPipeInclude(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ProviderPipeInclude(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if (!(pipe.pipe.logic instanceof LogicProvider)) {
			return;
		}
		((LogicProvider) pipe.pipe.logic).setFilterExcluded(getInteger() == 1);
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiProviderPipe) {
			((GuiProviderPipe) FMLClientHandler.instance().getClient().currentScreen).refreshInclude();
		}
	}
}

