package logisticspipes.request;

import java.util.ArrayList;

public class SubRequestGroup
{
	protected ArrayList<RequestTreeNode> nodes = new ArrayList<RequestTreeNode>();
	protected int totalPromisedItemCount = 0;
	public void addNode(RequestTreeNode node)
	{
		nodes.add(node);
		totalPromisedItemCount += node.getPromiseItemCount();
	}
	
	public ArrayList<RequestTreeNode> getNodes()
	{
		return nodes;
	}

	public int getTotalPromiseItemCount()
	{
		return totalPromisedItemCount;
	}
}
