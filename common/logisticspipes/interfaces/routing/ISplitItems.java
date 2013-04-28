package logisticspipes.interfaces.routing;

import logisticspipes.routing.IRouter;
import logisticspipes.utils.SinkReply;

public interface ISplitItems {
	public void setSplitGroup(int par1);
	public void setSplitAmount(int par1);
	public void subscribeToSplitting();
	public void unsubscribeFromSplitting();
	public int getSplitGroup();
	public int getSplitAmount();
}
