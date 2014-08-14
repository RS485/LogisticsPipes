package logisticspipes.proxy.buildcraft.gates.wrapperclasses;

import java.util.LinkedList;
import java.util.Map;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.IAction;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransport;

public class PipeWrapper extends Pipe<PipeTransport> {
	public final LogisticsTileGenericPipe tile;
	public final TilePipeWrapper wrapper;
	
	public PipeWrapper(LogisticsTileGenericPipe pipe) {
		super(new PipeTransport() {
			@Override
			public PipeType getPipeType() {
				return PipeType.STRUCTURE;
			}
		}, null);
		this.tile = pipe;
		this.setTile(wrapper = new TilePipeWrapper(this, tile));
	}

	public void updateWorld() {
		wrapper.updateWorld();
	}

	@Override
	public int getIconIndex(ForgeDirection arg0) {
		return 0;
	}

	@Override
	public IIconProvider getIconProvider() {
		return null;
	}

	@Override
	public void updateSignalState() {
		tile.pipe.updateSignalState();
	}

	@Override
	public boolean hasGate() {
		return tile.pipe.hasGate();
	}

	@Override
	public boolean hasGate(ForgeDirection side) {
		return tile.pipe.hasGate(side);
	}

	@Override
	protected void updateNeighbors(boolean needSelf) {
		tile.pipe.updateNeighbors(needSelf);
	}

	@Override
	public void dropItem(ItemStack stack) {
		tile.pipe.dropItem(stack);
	}

	@Override
	protected void actionsActivated(Map<IAction, Boolean> actions) {
		tile.pipe.bcPipePart.actionsActivated(actions);
	}

	@Override
	public LinkedList<IAction> getActions() {
		return (LinkedList<IAction>) tile.pipe.bcPipePart.getActions();
	}
}
