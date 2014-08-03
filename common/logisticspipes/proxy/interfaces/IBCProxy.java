package logisticspipes.proxy.interfaces;

import logisticspipes.transport.LPTravelingItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public interface IBCProxy {
	void resetItemRotation();
	boolean insertIntoBuildcraftPipe(TileEntity tile, LPTravelingItem item);
	boolean isIPipeTile(TileEntity tile);
	void registerPipeInformationProvider();
	void initProxy();
	boolean checkForPipeConnection(TileEntity with, ForgeDirection side);
	boolean checkConnectionOverride(TileEntity with, ForgeDirection side);
	boolean isMachineManagingSolids(TileEntity tile);
	boolean isMachineManagingFluids(TileEntity tile);
}
