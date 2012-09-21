package logisticspipes.transport;

import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.RoutedEntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import buildcraft.core.DefaultProps;
import buildcraft.transport.EntityData;
import buildcraft.transport.IItemTravelingHook;
import buildcraft.transport.PipeTransportItems;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class LogisticsItemTravelingHook implements IItemTravelingHook {
	private final World world;
	private final int xCoord;
	private final int yCoord;
	private final int zCoord;
	private final PipeTransportLogistics pipe;
	
	public LogisticsItemTravelingHook(World world, int xCoord, int yCoord, int zCoord, PipeTransportLogistics pipe) {
		this.world = world;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
		this.pipe = pipe;
	}
	
	@Override
	public void drop(PipeTransportItems pipe, EntityData data) {}

	@Override
	public void centerReached(PipeTransportItems pipe, EntityData data) {}

	@Override
	public void endReached(PipeTransportItems pipe, EntityData data, TileEntity tile) {
		if(MainProxy.isServer()) {
			if(data.item instanceof RoutedEntityItem) {
				RoutedEntityItem routed = (RoutedEntityItem) data.item;
				for(EntityPlayer player:MainProxy.getPlayerArround(world, xCoord, yCoord, zCoord, DefaultProps.NETWORK_UPDATE_RANGE)) {
					if(!routed.isKnownBy(player)) {
						PacketDispatcher.sendPacketToPlayer(pipe.createItemPacket(routed, data.orientation), (Player)player);
						if(routed.getDestination() != null) { 
							routed.addKnownPlayer(player);
						}
					}
				}
			}
		}
	}
}
