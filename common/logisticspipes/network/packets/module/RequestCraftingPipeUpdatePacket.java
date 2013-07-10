package logisticspipes.network.packets.module;

import cpw.mods.fml.common.network.Player;
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.oldpackets.PacketPipeUpdate;
import logisticspipes.network.packets.cpipe.CPipeSatelliteImportBack;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

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
		MainProxy.sendPacketToPlayer(new PacketPipeUpdate(NetworkConstants.PIPE_UPDATE, getPosX(), getPosY(), getPosZ(), ((CoreRoutedPipe) pipe.pipe).getLogisticsNetworkPacket()).getPacket(), (Player) player);
		if(pipe.pipe instanceof PipeItemsCraftingLogistics) {
			if(pipe.pipe.logic instanceof BaseLogicCrafting) {
				final CoordinatesPacket newpacket = PacketHandler.getPacket(CPipeSatelliteImportBack.class).setInventory(((BaseLogicCrafting) pipe.pipe.logic).getDummyInventory()).setPosX(pipe.xCoord).setPosY(pipe.yCoord).setPosZ(pipe.zCoord);
				MainProxy.sendPacketToPlayer(newpacket.getPacket(), (Player) player);
			}
		}
	}
}

