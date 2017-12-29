package logisticspipes.interfaces;

import java.util.Map;

import logisticspipes.utils.FluidIdentifier;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fluids.FluidStack;

public interface ISpecialTankAccessHandler extends ISpecialTankHandler {

	Map<FluidIdentifier, Long> getAvailableLiquid(TileEntity tile);

	FluidStack drainFrom(TileEntity tile, FluidIdentifier ident, Integer amount, boolean drain);
}
