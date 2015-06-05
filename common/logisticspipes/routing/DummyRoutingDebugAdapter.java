package logisticspipes.routing;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.PriorityQueue;

import logisticspipes.interfaces.IRoutingDebugAdapter;
import logisticspipes.interfaces.routing.IFilter;

public class DummyRoutingDebugAdapter implements IRoutingDebugAdapter {

	@Override
	public void start(PriorityQueue<ExitRoute> candidatesCost, ArrayList<EnumSet<PipeRoutingConnectionType>> closedSet, ArrayList<EnumMap<PipeRoutingConnectionType, List<List<IFilter>>>> filterList) {}

	@Override
	public void nextPipe(ExitRoute lowestCostNode) {}

	@Override
	public void handledPipe() {}

	@Override
	public void newCanidate(ExitRoute next) {}

	@Override
	public void stepOneDone() {}

	@Override
	public void stepTwoDone() {}

	@Override
	public void done() {}

	@Override
	public void init() {}

	@Override
	public void newFlagsForPipe(EnumSet<PipeRoutingConnectionType> newFlags) {}

	@Override
	public void filterList(EnumMap<PipeRoutingConnectionType, List<List<IFilter>>> filters) {}

	@Override
	public boolean independent() {
		return false;
	}

	@Override
	public boolean isDebug() {
		return false;
	}
}
