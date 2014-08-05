package logisticspipes.proxy.buildcraft.gates.wrapperclasses;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TileGenericPipe;

public class TilePipeWrapper extends TileGenericPipe {
	
	public final LogisticsTileGenericPipe tile;
	
	public TilePipeWrapper(PipeWrapper pipe) {
		this.tile = pipe.tile;
		this.xCoord = pipe.tile.xCoord;
		this.yCoord = pipe.tile.yCoord;
		this.zCoord = pipe.tile.zCoord;
		this.pipe = pipe;
		this.worldObj = pipe.tile.getWorldObj();
		try {
			Field field = TileGenericPipe.class.getDeclaredField("renderState");
			field.setAccessible(true);
			
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			
			field.set(null, new PipeRenderState() {
				@Override
				public boolean isGatePulsing() {
					return tile.renderState.isGatePulsing();
				}
			});
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
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
}
