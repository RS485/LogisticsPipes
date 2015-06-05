package logisticspipes.proxy.interfaces;

import java.util.List;

import net.minecraft.tileentity.TileEntity;

public interface IEnderIOProxy {

	public boolean isHyperCube(TileEntity tile);

	public boolean isTransceiver(TileEntity tile);

	public List<TileEntity> getConnectedHyperCubes(TileEntity tile);

	public List<TileEntity> getConnectedTransceivers(TileEntity tile);

	public boolean isSendAndReceive(TileEntity tile);

	public boolean isEnderIO();
}
