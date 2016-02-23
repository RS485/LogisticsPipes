package logisticspipes.proxy.specialtankhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logisticspipes.interfaces.ISpecialTankAccessHandler;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.FluidIdentifier;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;

public class AETankHandler implements ISpecialTankAccessHandler {

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean isType(TileEntity tile) {
		return tile instanceof ITileStorageMonitorable && tile instanceof IGridHost;
	}

	@Override
	public List<TileEntity> getBaseTilesFor(TileEntity tile) {
		List<TileEntity> tiles = new ArrayList<TileEntity>(1);
		if (tile instanceof IGridHost) {
			IGridHost host = (IGridHost) tile;
			IGridNode node = host.getGridNode(ForgeDirection.UNKNOWN);
			if (node != null) {
				TileEntity base = getBaseTileEntity(node);
				if (base != null) {
					tiles.add(base);
					return tiles;
				}
			}
		}
		tiles.add(tile);
		return tiles;
	}

	@SuppressWarnings("unused")
	@Override
	public Map<FluidIdentifier, Long> getAvailableLiquid(TileEntity tile) {
		Map<FluidIdentifier, Long> map = new HashMap<FluidIdentifier, Long>();
		if (tile instanceof ITileStorageMonitorable) {
			ITileStorageMonitorable mon = (ITileStorageMonitorable) tile;
			if (mon == null) {
				return map;
			}
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				MachineSource source = new MachineSource(new LPActionHost(((IGridHost) tile).getGridNode(dir)));
				IStorageMonitorable monitor = mon.getMonitorable(dir, source);
				if (monitor == null || monitor.getFluidInventory() == null) {
					continue;
				}
				IMEMonitor<IAEFluidStack> fluids = monitor.getFluidInventory();
				for (IAEFluidStack stack : fluids.getStorageList()) {
					if (SimpleServiceLocator.extraCellsProxy.canSeeFluidInNetwork(stack.getFluid())) {
						map.put(FluidIdentifier.get(stack.getFluid(), stack.getTagCompound() != null ? stack.getTagCompound().getNBTTagCompoundCopy() : null, null), stack.getStackSize());
					}
				}
				return map;
			}
		}
		return map;
	}

	@SuppressWarnings("unused")
	@Override
	public FluidStack drainFrom(TileEntity tile, FluidIdentifier ident, Integer amount, boolean drain) {
		if (tile instanceof ITileStorageMonitorable) {
			ITileStorageMonitorable mon = (ITileStorageMonitorable) tile;
			if (mon == null) {
				return null;
			}
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				MachineSource source = new MachineSource(new LPActionHost(((IGridHost) tile).getGridNode(dir)));
				IStorageMonitorable monitor = mon.getMonitorable(dir, source);
				if (monitor == null || monitor.getFluidInventory() == null) {
					continue;
				}
				IMEMonitor<IAEFluidStack> fluids = monitor.getFluidInventory();
				IAEFluidStack s = AEApi.instance().storage().createFluidStack(ident.makeFluidStack(amount));
				IAEFluidStack extracted = fluids.extractItems(s, drain ? Actionable.MODULATE : Actionable.SIMULATE, source);
				if (extracted == null) {
					return null;
				}
				return extracted.getFluidStack();
			}
		}
		return null;
	}

	private TileEntity getBaseTileEntity(IGridNode node) {
		IGrid grid = node.getGrid();
		if (grid == null) {
			return null;
		}
		IGridNode pivot = grid.getPivot();
		if (pivot == null) {
			return null;
		}
		IGridBlock block = pivot.getGridBlock();
		if (block == null) {
			return null;
		}
		DimensionalCoord coord = block.getLocation();
		if (coord == null) {
			return null;
		}
		World world = coord.getWorld();
		if (world == null) {
			return null;
		}
		return world.getTileEntity(coord.x, coord.y, coord.z);
	}

	private class LPActionHost implements IActionHost {

		public IGridNode node;

		public LPActionHost(IGridNode node) {
			this.node = node;
		}

		@Override
		public void securityBreak() {}

		@Override
		public IGridNode getGridNode(ForgeDirection paramForgeDirection) {
			return null;
		}

		@Override
		public AECableType getCableConnectionType(ForgeDirection paramForgeDirection) {
			return null;
		}

		@Override
		public IGridNode getActionableNode() {
			return node;
		}
	}
}
