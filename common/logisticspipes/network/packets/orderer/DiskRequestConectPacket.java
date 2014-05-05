package logisticspipes.network.packets.orderer;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.Player;

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
		if(pipe == null) {
			return;
		}
		if(pipe.pipe instanceof PipeItemsRequestLogisticsMk2) {
			if(((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk() != null) {
				if(!((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk().hasTagCompound()) {
					((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk().setTagCompound(new NBTTagCompound("tag"));
				}
			}
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(DiscContent.class).setStack(((PipeItemsRequestLogisticsMk2)pipe.pipe).getDisk()).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), (Player)player);
		}
		if(pipe.pipe instanceof PipeBlockRequestTable) {
			if(((PipeBlockRequestTable)pipe.pipe).diskInv.getStackInSlot(0) != null) {
				if(!((PipeBlockRequestTable)pipe.pipe).diskInv.getStackInSlot(0).hasTagCompound()) {
					((PipeBlockRequestTable)pipe.pipe).diskInv.getStackInSlot(0).setTagCompound(new NBTTagCompound("tag"));
				}
			}
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(DiscContent.class).setStack(((PipeBlockRequestTable)pipe.pipe).diskInv.getStackInSlot(0)).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), (Player)player);
		}
	}
}

