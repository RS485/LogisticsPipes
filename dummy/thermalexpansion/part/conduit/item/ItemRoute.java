package thermalexpansion.part.conduit.item;

import java.util.LinkedList;

public class ItemRoute implements Comparable<ItemRoute> {
	public LinkedList<Byte>	pathDirections	= new LinkedList();
	//public int					pathPos			= 0;
	public ConduitItem			endPoint;
	//public ConduitItem			startPoint;
	//public int					pathLength		= 0;
	//public boolean				routeFinished	= false;
	@Override public int compareTo(ItemRoute arg0) {return 0;}
	public ItemRoute copy() {return null;}
}
