package logisticspipes.proxy.interfaces;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public interface IEnderIOProxy {

	boolean isTransceiver(TileEntity tile);

	List<TileEntity> getConnectedTransceivers(TileEntity tile);

	boolean isSendAndReceive(TileEntity tile);

	boolean isEnderIO();

	boolean isItemConduit(TileEntity tile, EnumFacing dir);

	boolean isFluidConduit(TileEntity tile, EnumFacing dir);

	boolean isBundledPipe(TileEntity tile);
}
