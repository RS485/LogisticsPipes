package logisticspipes.proxy.specialtankhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logisticspipes.interfaces.ISpecialTankAccessHandler;
import logisticspipes.utils.FluidIdentifier;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.factory.TileTank;

public class BuildCraftTankHandler implements ISpecialTankAccessHandler {

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean isType(TileEntity tile) {
		return tile instanceof TileTank;
	}

	@Override
	public List<TileEntity> getBaseTilesFor(TileEntity tile) {
		List<TileEntity> tiles = new ArrayList<TileEntity>(1);
		tiles.add(((TileTank) tile).getBottomTank());
		return tiles;
	}

	@Override
	public Map<FluidIdentifier, Long> getAvailableLiquid(TileEntity tile) {
		Map<FluidIdentifier, Long> map = new HashMap<FluidIdentifier, Long>();
		FluidTankInfo[] tanks = ((IFluidHandler) tile).getTankInfo(ForgeDirection.UNKNOWN);
		for (FluidTankInfo tank : tanks) {
			if (tank == null) {
				continue;
			}
			FluidStack liquid;
			if ((liquid = tank.fluid) != null && liquid.getFluidID() != 0) {
				FluidIdentifier ident = FluidIdentifier.get(liquid);
				if (((IFluidHandler) tile).drain(ForgeDirection.UNKNOWN, 1, false) != null) {
					if (map.containsKey(ident)) {
						long addition = map.get(ident) + tank.fluid.amount;
						map.put(ident, addition);
					} else {
						map.put(ident, (long) tank.fluid.amount);
					}
				}
			}
		}
		return map;
	}

	@Override
	public FluidStack drainFrom(TileEntity tile, FluidIdentifier ident, Integer amount, boolean drain) {
		//workaround for BC5 and BC6 TileTank behavior before commit 08edcf1759c884cb95b66c53427bbd4b3f3d9751
		tile = ((TileTank) tile).getBottomTank();
		return ((IFluidHandler) tile).drain(ForgeDirection.UNKNOWN, ident.makeFluidStack(amount), drain);
	}
}
