package logisticspipes.network.packets.orderer;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class DiskDropPacket extends CoordinatesPacket {

	public DiskDropPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new DiskDropPacket(getId());
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
			((PipeItemsRequestLogisticsMk2) pipe.pipe).dropDisk();
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(DiscContent.class).setStack(((PipeItemsRequestLogisticsMk2) pipe.pipe).getDisk()).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), player);
		}
	}
}
