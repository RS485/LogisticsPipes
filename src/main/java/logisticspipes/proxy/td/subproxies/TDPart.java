package logisticspipes.proxy.td.subproxies;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.tiles.DuctToken;
import cofh.thermaldynamics.duct.tiles.TileDuctItem;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.td.LPDuctUnitItem;

public class TDPart implements ITDPart {

	public static boolean callSuperSideBlock = false;

	private final LogisticsTileGenericPipe pipe;
	private final TileDuctItem thermalDynamicsDucts;
	private final LPDuctUnitItem lpDuctUnit;

	public TDPart(LogisticsTileGenericPipe pipe) {
		this.pipe = pipe;
		thermalDynamicsDucts = new TileDuctItem.Basic.Transparent() {

			@Override
			public boolean isSideBlocked(int side) {
				if (callSuperSideBlock) {
					return super.isSideBlocked(side);
				}
				return lpDuctUnit.isLPBlockedSide(side, false);
			}
		};
		lpDuctUnit = new LPDuctUnitItem(thermalDynamicsDucts, TDDucts.itemBasic, pipe);
		thermalDynamicsDucts.addDuctUnits(DuctToken.ITEMS, lpDuctUnit);
	}

	@Override
	public TileEntity getInternalDuct() {
		return thermalDynamicsDucts;
	}

	@Override
	public void setWorld_LP(World world) {
		if (thermalDynamicsDucts != null) {
			thermalDynamicsDucts.setWorld(world);
			thermalDynamicsDucts.setPos(pipe.getPos());
			thermalDynamicsDucts.validate();
			//			thermalDynamicsDucts.onNeighborBlockChange();
		}
	}

	@Override
	public void invalidate() {
		if (thermalDynamicsDucts != null) {
			thermalDynamicsDucts.invalidate();
		}
	}

	@Override
	public void onChunkUnload() {
		if (thermalDynamicsDucts != null) {
			thermalDynamicsDucts.onChunkUnload();
		}
	}

	@Override
	public void scheduleNeighborChange() {
		if (thermalDynamicsDucts != null) {
			thermalDynamicsDucts.onNeighborBlockChange();
		}
	}

	@Override
	public void connectionsChanged() {
		if (thermalDynamicsDucts != null) {
			thermalDynamicsDucts.onNeighborBlockChange();
		}
	}

	@Override
	public boolean isLPSideBlocked(int i) {
		return lpDuctUnit.isLPBlockedSide(i, false);
	}

	@Override
	public void setPos(BlockPos pos) {
		thermalDynamicsDucts.setPos(pos);
	}
}
