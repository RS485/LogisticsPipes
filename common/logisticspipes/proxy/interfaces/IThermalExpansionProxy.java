package logisticspipes.proxy.interfaces;

import java.util.List;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public interface IThermalExpansionProxy {
	public boolean isTesseract(TileEntity tile);
	public List<TileEntity> getConnectedTesseracts(TileEntity tile);
	public boolean isTE();
}
