package logisticspipes.interfaces.routing;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import buildcraft.transport.EntityData;
import buildcraft.transport.TileGenericPipe;

public interface ISpecialTileConnection {
	public boolean init();
	public boolean isType(TileEntity tile);
	public List<TileGenericPipe> getConnections(TileEntity tile);
	public boolean needsInformationTransition();
	public void transmit(TileEntity tile, EntityData data);
}
