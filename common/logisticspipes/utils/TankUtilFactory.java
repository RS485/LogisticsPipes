package logisticspipes.utils;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;

import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import logisticspipes.interfaces.ISpecialTankAccessHandler;
import logisticspipes.interfaces.ISpecialTankHandler;
import logisticspipes.interfaces.ITankUtil;
import logisticspipes.proxy.SimpleServiceLocator;

public class TankUtilFactory {

	public static final TankUtilFactory INSTANCE = new TankUtilFactory();

	private TankUtilFactory() {}

	public ITankUtil getTankUtilForTE(BlockEntity tile, Direction dirOnEntity) {
		if (SimpleServiceLocator.specialTankHandler.hasHandlerFor(tile)) {
			ISpecialTankHandler handler = SimpleServiceLocator.specialTankHandler.getTankHandlerFor(tile);
			if (handler instanceof ISpecialTankAccessHandler) {
				if (tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dirOnEntity)) {
					IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dirOnEntity);
					if (fluidHandler != null) {
						return new SpecialTankUtil(fluidHandler, tile, (ISpecialTankAccessHandler) handler);
					}
				}
			}
		}

		if (tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dirOnEntity)) {
			IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dirOnEntity);
			if (fluidHandler != null) {
				return new TankUtil(fluidHandler);
			}
		}

		return null;
	}
}
