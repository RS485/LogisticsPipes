package logisticspipes.interfaces;

import java.util.Map;

import net.minecraft.block.entity.BlockEntity;

import net.minecraftforge.fluids.FluidStack;

import logisticspipes.utils.FluidIdentifier;

public interface ISpecialTankAccessHandler extends ISpecialTankHandler {

	Map<FluidIdentifier, Long> getAvailableLiquid(BlockEntity tile);

	FluidStack drainFrom(BlockEntity tile, FluidIdentifier ident, Integer amount, boolean drain);
}
