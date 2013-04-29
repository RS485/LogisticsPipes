package thermalexpansion.block.tesseract;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import thermalexpansion.api.tileentity.ITesseract;

public class TileTesseractItem extends TileEntity implements ITesseract {
	@Override
	public List<TileEntity> getValidInputLinks() {
		return null;
	}

	@Override
	public List<TileEntity> getValidOutputLinks() {
		return null;
	}
}
