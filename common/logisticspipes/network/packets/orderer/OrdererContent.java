package logisticspipes.network.packets.orderer;

import logisticspipes.gui.orderer.GuiOrderer;
import logisticspipes.network.abstractpackets.InventoryCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.client.FMLClientHandler;

public class OrdererContent extends InventoryCoordinatesPacket {

	public OrdererContent(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new OrdererContent(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiOrderer) {
			((GuiOrderer) FMLClientHandler.instance().getClient().currentScreen).handlePacket(getIdentList());
		}
	}
}

