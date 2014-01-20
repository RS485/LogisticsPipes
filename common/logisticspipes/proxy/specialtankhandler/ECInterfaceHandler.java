package logisticspipes.proxy.specialtankhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logisticspipes.interfaces.ISpecialTankAccessHandler;
import logisticspipes.utils.FluidIdentifier;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import extracells.integration.logisticspipes.IFluidNetworkAccess;
import extracells.util.SpecialFluidStack;

public class ECInterfaceHandler implements ISpecialTankAccessHandler {

	@Override
	public boolean init() {
		try {
			Class.forName("extracells.integration.logisticspipes.IFluidNetworkAccess");
			return true;
		} catch(Exception e) {
			return false;
		}
	}

	@Override
	public boolean isType(TileEntity tile) {
		return tile instanceof IFluidNetworkAccess;
	}

	@Override
	public List<TileEntity> getBaseTilesFor(TileEntity tile) {
		List<TileEntity> tiles = new ArrayList<TileEntity>(1);
		tiles.add(((IFluidNetworkAccess)tile).getNetworkController());
		return tiles;
	}

	@Override
	public Map<FluidIdentifier, Long> getAvailableLiquid(TileEntity tile) {
		Map<FluidIdentifier, Long> map = new HashMap<FluidIdentifier, Long>();
		List<SpecialFluidStack> list = ((IFluidNetworkAccess)tile).getFluidsInNetwork();
		if(list == null) return map;
		for(SpecialFluidStack stack:list) {
			map.put(FluidIdentifier.get(stack.getFluidStack()), stack.amount);
		}
		return map;
	}

	@Override
	public FluidStack drainFrom(TileEntity tile, FluidIdentifier ident, Integer amount, boolean drain) {
		long resultAmount = ((IFluidNetworkAccess)tile).drainFromNetwork(ident.makeFluidStack(amount), drain);
		FluidStack stack = ident.makeFluidStack((int) resultAmount);
		return stack;
	}

}
