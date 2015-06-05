package logisticspipes.interfaces;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.PriorityQueue;

import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.PipeRoutingConnectionType;

public interface IRoutingDebugAdapter {

	void start(PriorityQueue<ExitRoute> candidatesCost, ArrayList<EnumSet<PipeRoutingConnectionType>> closedSet, ArrayList<EnumMap<PipeRoutingConnectionType, List<List<IFilter>>>> filterList);

	void nextPipe(ExitRoute lowestCostNode);

	void handledPipe();

	void newCanidate(ExitRoute next);

	void stepOneDone();

	void stepTwoDone();

	void done();

	void init();

	void newFlagsForPipe(EnumSet<PipeRoutingConnectionType> newFlags);

	void filterList(EnumMap<PipeRoutingConnectionType, List<List<IFilter>>> filters);

	boolean independent();

	boolean isDebug();

}
