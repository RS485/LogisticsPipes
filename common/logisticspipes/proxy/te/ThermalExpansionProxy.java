package logisticspipes.proxy.te;

import java.util.List;

import logisticspipes.proxy.interfaces.IThermalExpansionProxy;
import net.minecraft.tileentity.TileEntity;
import thermalexpansion.api.tileentity.ITesseract;
import thermalexpansion.block.tesseract.TileTesseractItem;

public class ThermalExpansionProxy implements IThermalExpansionProxy {

	@Override
	public boolean isTesseract(TileEntity tile) {
		return tile instanceof TileTesseractItem;
	}

	@Override
	public List<TileEntity> getConnectedTesseracts(TileEntity tile) {
		return ((ITesseract)tile).getValidOutputLinks();
	}

	@Override
	public boolean isTE() {
		return true;
	}
}
