package logisticspipes.network.packets.module;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.cpipe.CPipeSatelliteImportBack;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;

public class RequestCraftingPipeUpdatePacket extends CoordinatesPacket {

	public RequestCraftingPipeUpdatePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new RequestCraftingPipeUpdatePacket(getId());
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if(!(pipe.pipe instanceof PipeItemsCraftingLogistics)) {
			Thread.dumpStack();
			return;
		}
		PipeItemsCraftingLogistics cpipe = (PipeItemsCraftingLogistics) pipe.pipe;
		MainProxy.sendPacketToPlayer(cpipe.getCPipePacket(), (Player) player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CPipeSatelliteImportBack.class).setInventory(((PipeItemsCraftingLogistics) pipe.pipe).getDummyInventory()).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), (Player) player);
	}
}

