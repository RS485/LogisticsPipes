package logisticspipes.network.packets.block;

import logisticspipes.gui.GuiStatistics;
import logisticspipes.network.abstractpackets.InventoryModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.client.FMLClientHandler;

public class AmountTaskSubGui extends InventoryModuleCoordinatesPacket {

	public AmountTaskSubGui(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiStatistics) {
			((GuiStatistics) FMLClientHandler.instance().getClient().currentScreen).handlePacket_1(getIdentList());
		}
	}

	@Override
	public ModernPacket template() {
		return new AmountTaskSubGui(getId());
	}
}
