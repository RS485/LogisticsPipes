package thermalexpansion.part.conduit;

import thermalexpansion.part.conduit.item.ConduitItem;

public interface IConduit {
	
	public abstract ConduitBase getConduit();
	
	public abstract boolean isItemConduit();
	
	public abstract ConduitItem getConduitItem();
}
