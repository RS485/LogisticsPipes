package logisticspipes.utils;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.interfaces.ISpecialTankHandler;
import logisticspipes.interfaces.ITankUtil;
import logisticspipes.proxy.SimpleServiceLocator;

public class TankUtilFactory {
	public ITankUtil getTankUtilForTE(TileEntity tile, EnumFacing dirOnEntity) {
		if (SimpleServiceLocator.specialTankHandler.hasHandlerFor(tile)) {
			ISpecialTankHandler handler = SimpleServiceLocator.specialTankHandler.getTankHandlerFor(tile);
			//TODO
		}
		//TODO
		return null;
	}
}
