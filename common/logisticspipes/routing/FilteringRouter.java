package logisticspipes.routing;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IFilteringRouter;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.utils.ItemIdentifier;
import net.minecraftforge.common.ForgeDirection;

public class FilteringRouter extends ServerRouter implements IFilteringRouter {
	
	private ForgeDirection side;
	
	public FilteringRouter(UUID id, int dimension, int xCoord, int yCoord, int zCoord, ForgeDirection dir) {
		super(id, dimension, xCoord, yCoord, zCoord);
		this.side = dir;
	}
	
	@Override
	public List<ExitRoute> getRouters() {
		if(LogisticsPipes.DEBUG && ForgeDirection.UNKNOWN.equals(side)) {
			throw new UnsupportedOperationException();
		}
		if(this.getPipe() instanceof PipeItemsFirewall) {
			return ((PipeItemsFirewall)this.getPipe()).getRouters(this);
		}
		return new ArrayList<ExitRoute>();
	}

	@Override
	public IFilter getFilter() {
		if(LogisticsPipes.DEBUG && ForgeDirection.UNKNOWN.equals(side)) {
			throw new UnsupportedOperationException();
		}
		if(this.getPipe() instanceof PipeItemsFirewall) {
			return ((PipeItemsFirewall)this.getPipe()).getFilter(this.getId(), this.getSimpleID());
		}
		return new IFilter() {
			@Override public UUID getUUID() {return UUID.randomUUID();}
			@Override public int getSimpleID() {return -1;}
			@Override public boolean isBlocked() {return true;}
			@Override public boolean isFilteredItem(ItemIdentifier item) {return false;}
			@Override public boolean blockProvider() {return false;}
			@Override public boolean blockCrafting() {return false;}
			@Override public boolean blockRouting() {return false;}
			@Override public boolean blockPower() {return true;}
		};
	}

	@Override
	public boolean isIdforOtherSide(int id) {
		if(this.getPipe() instanceof PipeItemsFirewall) {
			return ((PipeItemsFirewall)this.getPipe()).isIdforOtherSide(id);
		}
		return false;
	}

	@Override
	public boolean act(BitSet hasBeenProcessed, IRAction actor) {
		boolean hasBeenReset=false;
		if(!ForgeDirection.UNKNOWN.equals(side)) {
			CoreRoutedPipe pipe = this.getPipe();
			if(pipe != null) {
				IRouter router = pipe.getRouter();
				if(router != null) {
					hasBeenReset = router.act(hasBeenProcessed, actor);
				} else {
					throw new RuntimeException("Why is the router null? (" + this.toString() + ")");
				}
			} else {
				throw new RuntimeException("Why is the pipe null? (" + this.toString() + ")");
			}
		}
		if(hasBeenProcessed.get(this.simpleID))
			return hasBeenReset;
		hasBeenProcessed.set(this.simpleID);
		if(!actor.isInteresting(this))
			return hasBeenReset;
		if(actor.doTo(this)){
			hasBeenProcessed.clear();
			// don't need to worry about resetting the recursion, as we are the neighbor of our neighbor, and are no longer flagged as processed.
			hasBeenReset=true;
		}
		if(!ForgeDirection.UNKNOWN.equals(side)) {
			for(IRouter r : _adjacentRouter.keySet()) {
				hasBeenReset=hasBeenReset || r.act(hasBeenProcessed, actor);
			}
			for(IRouter r : _prevAdjacentRouter.keySet()) {
				hasBeenReset=hasBeenReset || r.act(hasBeenProcessed, actor);
			}
		}
		return hasBeenReset;
	}

	@Override
	public String toString() {
		String higher = super.toString().substring(6);
		StringBuilder string = new StringBuilder("Filtering");
		string.append(higher.substring(0, higher.length() - 1));
		string.append(", ");
		string.append(side);
		return string.append("}").toString();
	}
}
