package logisticspipes.interfaces.routing;

import java.util.List;

import net.minecraft.tileentity.TileEntity;

public interface ISpecialTileConnection {
	public boolean init();
	public boolean isType(TileEntity tile);
	public List<TileEntity> getConnections(TileEntity tile);
	public boolean needsInformationTransition();
	public void transmit(TileEntity tile, TravelingItem data);
}
