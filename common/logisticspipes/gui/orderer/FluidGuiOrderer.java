package logisticspipes.gui.orderer;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.RequestFluidOrdererRefreshPacket;
import logisticspipes.network.packets.orderer.SubmitFluidRequestPacket;
import logisticspipes.pipes.PipeFluidRequestLogistics;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.ItemDisplay;
import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;

public class FluidGuiOrderer extends GuiOrderer {

	public FluidGuiOrderer(PipeFluidRequestLogistics pipe, EntityPlayer entityPlayer) {
		super(pipe.getX(), pipe.getY(), pipe.getZ(), MainProxy.getDimensionForWorld(pipe.getWorld()), entityPlayer);
		_title = "Request Fluid";
		refreshItems();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		boolean setItemDisplay = itemDisplay == null;
		super.initGui();
		buttonList.add(new GuiButton(3, guiLeft + 10, bottom - 25, 46, 20, "Refresh")); // Refresh
		if (setItemDisplay) {
			itemDisplay = new ItemDisplay(this, mc.fontRenderer, this, this, guiLeft + 10, guiTop + 18, xSize - 20, ySize - 100, new int[] { 1, 1000, 16000, 100 }, false);
		}
		itemDisplay.reposition(guiLeft + 10, guiTop + 18, xSize - 20, ySize - 100);
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 0 && itemDisplay.getSelectedItem() != null) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SubmitFluidRequestPacket.class).setDimension(dimension).setStack(itemDisplay.getSelectedItem().getItem().makeStack(itemDisplay.getRequestCount())).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord));
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
