package logisticspipes.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IFilteringRouter;
import logisticspipes.pipes.PipeItemsFirewall;
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
		};
	}

	@Override
	public boolean isIdforOtherSide(int id) {
		if(this.getPipe() instanceof PipeItemsFirewall) {
			return ((PipeItemsFirewall)this.getPipe()).isIdforOtherSide(id);
		}
		return false;
	}
}
