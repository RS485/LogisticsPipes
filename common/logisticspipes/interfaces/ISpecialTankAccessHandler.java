package logisticspipes.interfaces;

import java.util.Map;

import logisticspipes.utils.FluidIdentifier;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fluids.FluidStack;

public interface ISpecialTankAccessHandler extends ISpecialTankHandler {

	public Map<FluidIdentifier, Long> getAvailableLiquid(TileEntity tile);

	public FluidStack drainFrom(TileEntity tile, FluidIdentifier ident, Integer amount, boolean drain);
}
