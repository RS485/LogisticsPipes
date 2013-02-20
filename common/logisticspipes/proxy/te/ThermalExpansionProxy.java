package logisticspipes.proxy.te;

import java.util.List;

import logisticspipes.proxy.interfaces.IThermalExpansionProxy;
import net.minecraft.tileentity.TileEntity;
import thermalexpansion.transport.tileentity.TileTeleportItem;

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
