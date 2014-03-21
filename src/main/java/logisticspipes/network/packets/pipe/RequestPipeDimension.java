package logisticspipes.network.packets.pipe;

import logisticspipes.gui.orderer.GuiOrderer;
import logisticspipes.network.abstractpackets.IntegerPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.client.FMLClientHandler;

public class RequestPipeDimension extends IntegerPacket {

	public RequestPipeDimension(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new RequestPipeDimension(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if(FMLClientHandler.instance().getClient().currentScreen instanceof GuiOrderer) {
			((GuiOrderer)FMLClientHandler.instance().getClient().currentScreen).dimension = getInteger();
			((GuiOrderer)FMLClientHandler.instance().getClient().currentScreen).refreshItems();
		} else {
			GuiOrderer.dimensioncache = getInteger();
			GuiOrderer.cachetime = System.currentTimeMillis();
		}
	}
}

