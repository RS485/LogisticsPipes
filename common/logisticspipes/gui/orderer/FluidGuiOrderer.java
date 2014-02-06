package logisticspipes.gui.orderer;

import java.util.Iterator;

import logisticspipes.gui.orderer.GuiOrderer.LoadedItem;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.RequestFluidOrdererRefreshPacket;
import logisticspipes.network.packets.orderer.RequestSubmitPacket;
import logisticspipes.network.packets.orderer.SubmitFluidRequestPacket;
import logisticspipes.pipes.PipeFluidRequestLogistics;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;

public class FluidGuiOrderer extends GuiOrderer {

	public FluidGuiOrderer(PipeFluidRequestLogistics pipe, EntityPlayer entityPlayer) {
		super(pipe.getX(), pipe.getY(), pipe.getZ(), MainProxy.getDimensionForWorld(pipe.getWorld()), entityPlayer);
		refreshItems();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(new GuiButton(BUTTON_REFRESH, guiLeft + 10, bottom - 25, 46, 20, "Refresh")); // Refresh
	}
	
	@Override
	public void requestItems() {
		if (requestCount > 0) {
			Iterator<LoadedItem> iter = this.loadedItems.iterator();

			while (iter.hasNext()) {
				LoadedItem item = iter.next();

				if (item.isSelected() && item.isDisplayed()) {
					MainProxy.sendPacketToServer(PacketHandler.getPacket(SubmitFluidRequestPacket.class).setDimension(dimension).setStack(item.getStack().getItem().makeStack(requestCount)).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord));
				}
			}
		}
	}
	
	@Override
	protected int getAmountChangeMode(int step) {
		if(step == 1) {
			return 1;
		} else if(step == 2) {
			return 1000;
		} else if(step == 4) {
			return 100;
		} else {
			return 16000;
		}
	}
	
	@Override
	protected int getStackAmount() {
		return 1000;
	}
	
	@Override
	public void refreshItems() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestFluidOrdererRefreshPacket.class).setInteger(dimension).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord));
	}

	@Override
	public void specialItemRendering(ItemIdentifier item, int x, int y) {}
}
