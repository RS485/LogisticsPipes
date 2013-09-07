package logisticspipes.interfaces.routing;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;

public interface ISpecialTileConnection {
	public boolean init();
	public boolean isType(TileEntity tile);
	public List<TileGenericPipe> getConnections(TileEntity tile);
	public boolean needsInformationTransition();
	public void transmit(TileEntity tile, TravelingItem data);
}
