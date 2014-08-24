package logisticspipes.proxy.buildcraft.bc60.gates.wrapperclasses;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.tileentity.TileEntity;
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
		try {
			Field field = TileGenericPipe.class.getDeclaredField("renderState");
			field.setAccessible(true);
			
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			
			field.set(this, new PipeRenderState() {
				@Override
				public boolean isGatePulsing() {
					return tile.renderState.bcRenderState.isGatePulsing();
				}
			});
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void updateWorld() {
		this.worldObj = tile.getWorldObj();
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
