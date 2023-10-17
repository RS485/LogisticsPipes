package logisticspipes.gui.orderer;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.RequestFluidOrdererRefreshPacket;
import logisticspipes.network.packets.orderer.SubmitFluidRequestPacket;
import logisticspipes.pipes.PipeFluidRequestLogistics;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.ItemDisplay;
import logisticspipes.utils.item.ItemIdentifier;

public class FluidGuiOrderer extends GuiOrderer {

	public FluidGuiOrderer(PipeFluidRequestLogistics pipe, EntityPlayer entityPlayer) {
		super(pipe.getX(), pipe.getY(), pipe.getZ(), pipe.getWorld().provider.getDimension(), entityPlayer);
		_title = "Request Fluid";
		refreshItems();
	}

	@Override
	public void initGui() {
		boolean setItemDisplay = itemDisplay == null;
		super.initGui();
		buttonList.add(new GuiButton(3, guiLeft + 10, bottom - 25, 46, 20, "Refresh")); // Refresh
		if (setItemDisplay) {
			itemDisplay = new ItemDisplay(this, fontRenderer, this, this, guiLeft + 10, guiTop + 18, xSize - 20, ySize - 100, xCenter, bottom - 24, 49, new int[] { 1, 1000, 16000, 100 }, false);
		}
		itemDisplay.reposition(guiLeft + 10, guiTop + 18, xSize - 20, ySize - 100, xCenter, bottom - 24);
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) throws IOException {
		if (guibutton.id == 0 && itemDisplay.getSelectedItem() != null) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SubmitFluidRequestPacket.class).setStack(itemDisplay.getSelectedItem().getItem().makeStack(itemDisplay.getRequestCount())).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord).setDimension(dimension));
			refreshItems();
		} else {
			super.actionPerformed(guibutton);
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
