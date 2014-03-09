package logisticspipes.network.packets.modules;

import logisticspipes.gui.modules.GuiProvider;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.client.FMLClientHandler;

public class ProviderModuleInclude extends IntegerCoordinatesPacket {

	public ProviderModuleInclude(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ProviderModuleInclude(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiProvider) {
			((GuiProvider) FMLClientHandler.instance().getClient().currentScreen).handleModuleIncludeRecive(getInteger());
		}
	}
}

