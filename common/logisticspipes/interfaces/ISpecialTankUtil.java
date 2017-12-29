package logisticspipes.interfaces;

import net.minecraft.tileentity.TileEntity;

public interface ISpecialTankUtil extends ITankUtil {
	public TileEntity getTileEntity();

	ISpecialTankAccessHandler getSpecialHandler();
}
