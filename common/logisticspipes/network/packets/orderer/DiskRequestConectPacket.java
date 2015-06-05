package logisticspipes.network.packets.orderer;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class DiskRequestConectPacket extends CoordinatesPacket {

	public DiskRequestConectPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new DiskRequestConectPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if (pipe.pipe instanceof PipeItemsRequestLogisticsMk2) {
			if (((PipeItemsRequestLogisticsMk2) pipe.pipe).getDisk() != null) {
				if (((PipeItemsRequestLogisticsMk2) pipe.pipe).getDisk().getItem().equals(LogisticsPipes.LogisticsItemDisk)) {
					if (!((PipeItemsRequestLogisticsMk2) pipe.pipe).getDisk().hasTagCompound()) {
						((PipeItemsRequestLogisticsMk2) pipe.pipe).getDisk().setTagCompound(new NBTTagCompound());
					}
				}
			}
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(DiscContent.class).setStack(((PipeItemsRequestLogisticsMk2) pipe.pipe).getDisk()).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), player);
		}
		if (pipe.pipe instanceof PipeBlockRequestTable) {
			if (((PipeBlockRequestTable) pipe.pipe).diskInv.getStackInSlot(0) != null) {
				if (((PipeBlockRequestTable) pipe.pipe).diskInv.getStackInSlot(0).getItem().equals(LogisticsPipes.LogisticsItemDisk)) {
					if (!((PipeBlockRequestTable) pipe.pipe).diskInv.getStackInSlot(0).hasTagCompound()) {
						((PipeBlockRequestTable) pipe.pipe).diskInv.getStackInSlot(0).setTagCompound(new NBTTagCompound());
					}
				}
			}
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(DiscContent.class).setStack(((PipeBlockRequestTable) pipe.pipe).diskInv.getStackInSlot(0)).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), player);
		}
	}
}
