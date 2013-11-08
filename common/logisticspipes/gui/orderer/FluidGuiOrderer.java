package logisticspipes.gui.orderer;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.RequestFluidOrdererRefreshPacket;
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
		buttonList.add(new GuiButton(3, guiLeft + 10, bottom - 25, 46, 20, "Refresh")); // Refresh
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 0 && selectedItem != null){
			if(editsearch) {
				editsearchb = false;
			}
			clickWasButton = true;
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SubmitFluidRequestPacket.class).setDimension(dimension).setStack(selectedItem.getItem().makeStack(requestCount)).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord));
			refreshItems();
		} else {
			super.actionPerformed(guibutton);
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
	protected boolean isShiftPageChange() {
		return false;
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
