package logisticspipes.network.packets.orderer;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.LPItems;
import logisticspipes.network.abstractpackets.ItemPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;

@StaticResolve
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
		final LogisticsTileGenericPipe tile = this.getPipe(player.world);
		if (tile == null) {
			return;
		}
		if (tile.pipe instanceof PipeItemsRequestLogisticsMk2) {
			if (MainProxy.isServer(tile.getWorld())) {
				if (!((PipeItemsRequestLogisticsMk2) tile.pipe).getDisk().isEmpty() && ((PipeItemsRequestLogisticsMk2) tile.pipe).getDisk().getItem().equals(LPItems.disk)) {
					if (!getStack().isEmpty() && getStack().getItem().equals(LPItems.disk)) {
						((PipeItemsRequestLogisticsMk2) tile.pipe).getDisk().setTagCompound(getStack().getTagCompound());
					}
				}
			} else {
				((PipeItemsRequestLogisticsMk2) tile.pipe).setDisk(getStack());
			}
		}
		if (tile.pipe instanceof PipeBlockRequestTable) {
			if (MainProxy.isServer(tile.getWorld())) {
				if (!((PipeBlockRequestTable) tile.pipe).diskInv.getStackInSlot(0).isEmpty() && ((PipeBlockRequestTable) tile.pipe).diskInv.getStackInSlot(0).getItem().equals(LPItems.disk)) {
					if (!getStack().isEmpty() && getStack().getItem().equals(LPItems.disk)) {
						((PipeBlockRequestTable) tile.pipe).diskInv.getStackInSlot(0).setTagCompound(getStack().getTagCompound());
					}
				}
			} else {
				((PipeBlockRequestTable) tile.pipe).diskInv.setInventorySlotContents(0, getStack());
			}
		}
	}
}
