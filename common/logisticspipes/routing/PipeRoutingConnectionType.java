package logisticspipes.routing;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;

public enum PipeRoutingConnectionType{
	passedThroughDiamond,
	passedThroughIronForwards,
	passedThroughIronBackwards,
	passedThroughObsidian,
	blocksPowerFlow,
	blocksItemFlow;
	public static int encode(EnumSet<PipeRoutingConnectionType> set) {
	    int ret = 0;

	    for (PipeRoutingConnectionType val : set) {
	        ret |= 1 << val.ordinal();
	    }

	    return ret;
	}

	public static EnumSet<PipeRoutingConnectionType> decode(int code) {
	    PipeRoutingConnectionType[] values = (PipeRoutingConnectionType[]) PipeRoutingConnectionType.values();
		EnumSet<PipeRoutingConnectionType> result = EnumSet.noneOf(PipeRoutingConnectionType.class);
		while (code != 0) {
		    int ordinal = Integer.numberOfTrailingZeros(code);
		    code ^= Integer.lowestOneBit(code);
		    result.add(values[ordinal]);
		}
		return result;
	}

}
