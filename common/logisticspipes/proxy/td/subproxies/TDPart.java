package logisticspipes.proxy.td.subproxies;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.td.LPItemDuct;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraft.util.EnumFacing;

import cofh.thermaldynamics.core.TickHandler;

public class TDPart implements ITDPart {

	private final LogisticsTileGenericPipe pipe;
	private final LPItemDuct[] thermalDynamicsDucts;

	public TDPart(LogisticsTileGenericPipe pipe) {
		this.pipe = pipe;
		thermalDynamicsDucts = new LPItemDuct[6];
	}

	@Override
	public TileEntity getInternalDuctForSide(EnumFacing opposite) {
		if (opposite.ordinal() < 6) {
			LPItemDuct duct = thermalDynamicsDucts[opposite.ordinal()];
			if (duct == null) {
				duct = thermalDynamicsDucts[opposite.ordinal()] = new LPItemDuct(pipe, opposite);
				if (MainProxy.isServer(pipe.getWorld())) {
					TickHandler.addMultiBlockToCalculate(duct);
				}
				duct.setWorldObj(pipe.getWorld());
				duct.getPos().getX() = pipe.getPos().getX();
				duct.getPos().getY() = pipe.getPos().getY();
				duct.getPos().getZ() = pipe.getPos().getZ();
				duct.validate();
				LPPosition pos = new LPPosition((TileEntity) pipe);
				pos.moveForward(opposite);
				duct.onNeighborTileChange(pos.getX(), pos.getY(), pos.getZ());
			}
			return duct;
		}
		return null;
	}

	@Override
	public void setWorldObj_LP(World world) {
		for (int i = 0; i < 6; i++) {
			if (thermalDynamicsDucts[i] != null) {
				thermalDynamicsDucts[i].setWorldObj(world);
				thermalDynamicsDucts[i].getPos().getX() = pipe.pos().getX();
				thermalDynamicsDucts[i].yCoord = pipe.yCoord;
				thermalDynamicsDucts[i].zCoord = pipe.zCoord;
			}
		}
	}

	@Override
	public void invalidate() {
		for (int i = 0; i < 6; i++) {
			if (thermalDynamicsDucts[i] != null) {
				thermalDynamicsDucts[i].invalidate();
			}
		}
	}

	@Override
	public void onChunkUnload() {
		for (int i = 0; i < 6; i++) {
			if (thermalDynamicsDucts[i] != null) {
				thermalDynamicsDucts[i].onChunkUnload();
			}
		}
	}

	@Override
	public void scheduleNeighborChange() {
		for (int i = 0; i < 6; i++) {
			if (thermalDynamicsDucts[i] != null) {
				thermalDynamicsDucts[i].onNeighborBlockChange();
			}
		}
	}

	@Override
	public void connectionsChanged() {
		for (int i = 0; i < 6; i++) {
			if (thermalDynamicsDucts[i] != null && thermalDynamicsDucts[i].myGrid != null) {
				thermalDynamicsDucts[i].myGrid.destroyAndRecreate();
			}
		}
	}

}
