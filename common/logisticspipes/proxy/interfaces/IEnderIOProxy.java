package logisticspipes.proxy.interfaces;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public interface IEnderIOProxy {

	public boolean isHyperCube(TileEntity tile);

	public boolean isTransceiver(TileEntity tile);

	public List<TileEntity> getConnectedHyperCubes(TileEntity tile);

	public List<TileEntity> getConnectedTransceivers(TileEntity tile);

	public boolean isSendAndReceive(TileEntity tile);

	public boolean isEnderIO();

	boolean isItemConduit(TileEntity tile, ForgeDirection dir);

	boolean isFluidConduit(TileEntity tile, ForgeDirection dir);

	boolean isBundledPipe(TileEntity tile);
}
