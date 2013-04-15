package logisticspipes.proxy.te;

import java.util.List;

import logisticspipes.proxy.interfaces.IThermalExpansionProxy;
import net.minecraft.tileentity.TileEntity;
//import thermalexpansion.transport.tileentity.TileTeleportItem;

public class ThermalExpansionProxy implements IThermalExpansionProxy {

	@Override
	public boolean isTesseract(TileEntity tile) {
		//TODO TE related
		//return tile instanceof TileTeleportItem;
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TileEntity> getConnectedTesseracts(TileEntity tile) {
		//TODO TE related
		//return ((TileTeleportItem)tile).getValidOutputs();
		return null;
	}

	@Override
	public boolean isTE() {
		return true;
	}
}
