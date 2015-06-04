package logisticspipes.proxy.buildcraft.subproxies;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import buildcraft.transport.PipeTransportFluids;

public class LPBCPipeTransportsFluids extends PipeTransportFluids {

	private final LogisticsTileGenericPipe pipe;
	
	public LPBCPipeTransportsFluids(LogisticsTileGenericPipe pipe) {
		this.pipe = pipe;
	}
	
}
