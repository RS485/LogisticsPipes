package logisticspipes.interfaces;

import net.minecraft.tileentity.TileEntity;

public interface ISpecialTankUtil extends ITankUtil {

	TileEntity getTileEntity();

	ISpecialTankAccessHandler getSpecialHandler();
}
