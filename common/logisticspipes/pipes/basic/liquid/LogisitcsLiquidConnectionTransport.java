package logisticspipes.pipes.basic.liquid;

import logisticspipes.transport.PipeLiquidTransportLogistics;
import buildcraft.transport.PipeTransport;
import buildcraft.transport.PipeTransportLiquids;

public class LogisitcsLiquidConnectionTransport extends PipeTransportLiquids {

	@Override
	public boolean allowsConnect(PipeTransport with) {
		return super.allowsConnect(with) || with instanceof PipeLiquidTransportLogistics;
	}
}
