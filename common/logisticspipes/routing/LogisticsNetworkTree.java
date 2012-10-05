package logisticspipes.routing;

import java.util.LinkedHashMap;

public class LogisticsNetworkTree {
	public IRouter router;
	public LinkedHashMap<LogisticsNetworkTree, ExitRoute> connection;
	public INetworkResistanceFilter filter;
	
	public LogisticsNetworkTree(IRouter router, LinkedHashMap<LogisticsNetworkTree, ExitRoute> connection, INetworkResistanceFilter filter) {
		this.router = router;
		this.connection = connection;
		this.filter = filter;
	}
}
