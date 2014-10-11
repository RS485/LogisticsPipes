package logisticspipes.proxy.buildcraft.bc61.gates.wrapperclasses;

import java.util.LinkedList;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.buildcraft.bc61.subproxies.BCRenderState;
import logisticspipes.utils.ReflectionHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftCore;
import buildcraft.api.gates.IOverrideDefaultTriggers;
import buildcraft.api.gates.ITrigger;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TileGenericPipe;

public class TilePipeWrapper extends TileGenericPipe implements IOverrideDefaultTriggers {
	
	public final LogisticsTileGenericPipe tile;
	
	public TilePipeWrapper(PipeWrapper pipe, final LogisticsTileGenericPipe tile) {
		this.tile = tile;
		this.xCoord = tile.xCoord;
		this.yCoord = tile.yCoord;
		this.zCoord = tile.zCoord;
		this.pipe = pipe;
		this.redstoneInput = tile.redstoneInput;
		this.redstoneInputSide = tile.redstoneInputSide;
		BCRenderState bcRenderState = ((BCRenderState)tile.renderState.bcRenderState.getOriginal());
		try {
			ReflectionHelper.setFinalField(PipeRenderState.class, "gateMatrix", this.renderState, bcRenderState.gateMatrix);
			ReflectionHelper.setFinalField(PipeRenderState.class, "robotStationMatrix", this.renderState, bcRenderState.robotStationMatrix);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void updateWorld() {
		this.worldObj = tile.getWorldObj();
	}

	@Override
	public World getWorld() {
		return tile.getWorldObj();
	}

	@Override
	public void scheduleNeighborChange() {
		tile.scheduleNeighborChange();
	}

	@Override
	public TileEntity getTile(ForgeDirection to) {
		return tile.getTile(to);
	}

	@Override
	public void scheduleRenderUpdate() {
		tile.scheduleRenderUpdate();
	}
	
	public LinkedList<ITrigger> getTriggers() {
		LinkedList<ITrigger> result = new LinkedList<ITrigger>(); //TODO
		if (BlockGenericPipe.isFullyDefined(pipe) && pipe.hasGate()) {
			result.add(BuildCraftCore.triggerRedstoneActive);
			result.add(BuildCraftCore.triggerRedstoneInactive);
		}
		return result;
	}
}
