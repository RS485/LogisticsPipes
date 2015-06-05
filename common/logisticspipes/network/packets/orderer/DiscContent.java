package logisticspipes.network.packets.orderer;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.abstractpackets.ItemPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;

import net.minecraft.entity.player.EntityPlayer;

public class DiscContent extends ItemPacket {

	public DiscContent(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new DiscContent(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe tile = this.getPipe(player.worldObj);
		if (tile == null) {
			return;
		}
		if (tile.pipe instanceof PipeItemsRequestLogisticsMk2) {
			if (MainProxy.isServer(tile.getWorld())) {
				if (((PipeItemsRequestLogisticsMk2) tile.pipe).getDisk() != null && ((PipeItemsRequestLogisticsMk2) tile.pipe).getDisk().getItem().equals(LogisticsPipes.LogisticsItemDisk)) {
					if (getStack() != null && getStack().getItem().equals(LogisticsPipes.LogisticsItemDisk)) {
						((PipeItemsRequestLogisticsMk2) tile.pipe).getDisk().setTagCompound(getStack().getTagCompound());
					}
				}
			} else {
				((PipeItemsRequestLogisticsMk2) tile.pipe).setDisk(getStack());
			}
		}
		if (tile.pipe instanceof PipeBlockRequestTable) {
			if (MainProxy.isServer(tile.getWorld())) {
				if (((PipeBlockRequestTable) tile.pipe).diskInv.getStackInSlot(0) != null && ((PipeBlockRequestTable) tile.pipe).diskInv.getStackInSlot(0).getItem().equals(LogisticsPipes.LogisticsItemDisk)) {
					if (getStack() != null && getStack().getItem().equals(LogisticsPipes.LogisticsItemDisk)) {
						((PipeBlockRequestTable) tile.pipe).diskInv.getStackInSlot(0).setTagCompound(getStack().getTagCompound());
					}
				}
			} else {
				((PipeBlockRequestTable) tile.pipe).diskInv.setInventorySlotContents(0, getStack());
			}
		}
	}
}
