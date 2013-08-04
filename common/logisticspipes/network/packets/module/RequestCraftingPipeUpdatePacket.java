package logisticspipes.network.packets.module;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.cpipe.CPipeSatelliteImportBack;
import logisticspipes.network.packets.pipe.PipeUpdate;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;

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
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if( !(pipe.pipe instanceof CoreRoutedPipe)) return;
//TODO 	MainProxy.sendPacketToPlayer(new PacketPipeUpdate(NetworkConstants.PIPE_UPDATE, getPosX(), getPosY(), getPosZ(), ((CoreRoutedPipe) pipe.pipe).getLogisticsNetworkPacket()).getPacket(), (Player) player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(PipeUpdate.class).setPayload(((CoreRoutedPipe) pipe.pipe).getLogisticsNetworkPacket()).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
		if(pipe.pipe instanceof PipeItemsCraftingLogistics) {
			if(pipe.pipe instanceof PipeItemsCraftingLogistics) {
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CPipeSatelliteImportBack.class).setInventory(((PipeItemsCraftingLogistics) pipe.pipe).getDummyInventory()).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord), (Player) player);
			}
		}
	}
}

