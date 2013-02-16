package logisticspipes.proxy.te;

import java.util.List;

import thermalexpansion.transport.tileentity.TileTeleportItem;

import net.minecraft.tileentity.TileEntity;
import logisticspipes.proxy.interfaces.IThermalExpansionProxy;

public class ThermalExpansionProxy implements IThermalExpansionProxy {

	@Override
	public boolean isTesseract(TileEntity tile) {
		return tile instanceof TileTeleportItem;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TileEntity> getConnectedTesseracts(TileEntity tile) {
		return ((TileTeleportItem)tile).getValidOutputs();
	}

	@Override
	public boolean isTE() {
		return true;
	}
}
