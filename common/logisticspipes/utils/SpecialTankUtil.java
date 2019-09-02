package logisticspipes.utils;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fluids.capability.IFluidHandler;

import logisticspipes.interfaces.ISpecialTankAccessHandler;
import logisticspipes.interfaces.ISpecialTankUtil;

public class SpecialTankUtil extends TankUtil implements ISpecialTankUtil {

	private TileEntity tile;
	private ISpecialTankAccessHandler handler;

	public SpecialTankUtil(IFluidHandler fluid, TileEntity tile, ISpecialTankAccessHandler handler) {
		super(fluid);
		this.tile = tile;
		this.handler = handler;
	}

	@Override
	public TileEntity getTileEntity() {
		return tile;
	}

	@Override
	public ISpecialTankAccessHandler getSpecialHandler() {
		return handler;
	}
}
