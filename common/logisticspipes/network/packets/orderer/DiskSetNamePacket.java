package logisticspipes.network.packets.orderer;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.StringCoordinatesPacket;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class DiskSetNamePacket extends StringCoordinatesPacket {

	public DiskSetNamePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new DiskSetNamePacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if (pipe.pipe instanceof PipeItemsRequestLogisticsMk2) {
			if (((PipeItemsRequestLogisticsMk2) pipe.pipe).getDisk() == null) {
				return;
			}
			if (!((PipeItemsRequestLogisticsMk2) pipe.pipe).getDisk().getItem().equals(LogisticsPipes.LogisticsItemDisk)) {
				return;
			}
			if (!((PipeItemsRequestLogisticsMk2) pipe.pipe).getDisk().hasTagCompound()) {
				((PipeItemsRequestLogisticsMk2) pipe.pipe).getDisk().setTagCompound(new NBTTagCompound());
			}
			NBTTagCompound nbt = ((PipeItemsRequestLogisticsMk2) pipe.pipe).getDisk().getTagCompound();
			nbt.setString("name", getString());
		}
	}
}
