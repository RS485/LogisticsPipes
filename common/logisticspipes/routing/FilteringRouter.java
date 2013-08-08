package logisticspipes.routing;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IFilteringRouter;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.utils.ItemIdentifier;
import lombok.Getter;
import net.minecraftforge.common.ForgeDirection;

public class FilteringRouter extends ServerRouter implements IFilteringRouter {
	
	@Getter
	private ForgeDirection side;
	
	private final IRouter[] _otherRouters;
	
	public FilteringRouter(UUID id, int dimension, int xCoord, int yCoord, int zCoord, ForgeDirection dir, IRouter[] otherRouters) {
		super(id, dimension, xCoord, yCoord, zCoord);
		this.side = dir;
		_otherRouters = otherRouters;
	}
	
	@Override
	public List<ExitRoute> getRouters() {
		if(LogisticsPipes.DEBUG && ForgeDirection.UNKNOWN.equals(side)) {
			throw new UnsupportedOperationException(this.toString());
		}

		List<ExitRoute> list = new ArrayList<ExitRoute>();
		for(int i=0; i<6; i++) {
			if(_otherRouters[i].equals(this)) continue;
			List<ExitRoute> nodes = _otherRouters[i].getIRoutersByCost();
			list.addAll(nodes);
		}
		Collections.sort(list);
		return list;
	}

	@Override
	public IFilter getFilter() {
		if(LogisticsPipes.DEBUG && ForgeDirection.UNKNOWN.equals(side)) {
			throw new UnsupportedOperationException(this.toString());
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
		for(int i=0; i<6; i++) {
			if(_otherRouters[i].getSimpleID() == id)
				return true;
		}
		return false;
	}

	@Override
	public boolean act(BitSet hasBeenProcessed, IRAction actor) {
		boolean hasBeenReset=false;
		if(hasBeenProcessed.get(this.simpleID))
			return hasBeenReset;
		hasBeenProcessed.set(this.simpleID);
		if(!ForgeDirection.UNKNOWN.equals(side)) {
			IRouter router = _otherRouters[6];
			if(router != null) {
				if(router instanceof FilteringRouter) {
					if(ForgeDirection.UNKNOWN.equals(((FilteringRouter)router).side)) {
						hasBeenReset = router.act(hasBeenProcessed, actor);
					} else {
						throw new RuntimeException("Why is the FilteringRouter not centered? (" + router.toString() + ")");
					}
				} else {
					throw new RuntimeException("Why is the router not an FilteringRouter? (" + router.toString() + ")");
				}
			} else {
				throw new RuntimeException("Why is the router null? (" + this.toString() + ")");
			}
		}
		if(!actor.isInteresting(this))
			return hasBeenReset;
		if(actor.doTo(this)){
			hasBeenProcessed.clear();
			// don't need to worry about resetting the recursion, as we are the neighbor of our neighbor, and are no longer flagged as processed.
			hasBeenReset=true;
		}
		for(IRouter r : _adjacentRouter.keySet()) {
			hasBeenReset=hasBeenReset || r.act(hasBeenProcessed, actor);
		}
		for(IRouter r : _prevAdjacentRouter.keySet()) {
			hasBeenReset=hasBeenReset || r.act(hasBeenProcessed, actor);
		}
		return hasBeenReset;
	}

	@Override
	public IRouter getRouter(ForgeDirection insertOrientation) {
		if(_otherRouters.length <= insertOrientation.ordinal()) return null;
		return _otherRouters[insertOrientation.ordinal()];
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
