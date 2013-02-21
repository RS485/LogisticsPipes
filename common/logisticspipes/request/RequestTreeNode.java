package logisticspipes.request;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.utils.ItemIdentifierStack;

public class RequestTreeNode {

	public RequestTreeNode(ItemIdentifierStack item, IRequestItems requester, RequestTreeNode parentNode) {
		this.request = item;
		this.target = requester;
		this.parentNode=parentNode;
		if(parentNode!=null)
			parentNode.subRequests.add(this);
	}

	
	protected final IRequestItems target;
	protected final ItemIdentifierStack request;
	protected final RequestTreeNode parentNode;
	protected List<RequestTreeNode> subRequests = new ArrayList<RequestTreeNode>();
	protected List<LogisticsPromise> promises = new ArrayList<LogisticsPromise>();
	protected List<LogisticsExtraPromise> extrapromises = new ArrayList<LogisticsExtraPromise>();
	protected SortedSet<CraftingTemplate> usedCrafters= new TreeSet<CraftingTemplate>();
	protected CraftingTemplate lastCrafterTried = null;
	
	private int promiseItemCount = 0;

	public boolean isCrafterUsed(CraftingTemplate test) {
		if(!usedCrafters.isEmpty() && usedCrafters.contains(test))
			return true;
		if(parentNode==null)
			return false;
		return parentNode.isCrafterUsed(test);
	}
	
	// returns false if the crafter was already on the list.
	public boolean declareCrafterUsed(CraftingTemplate test) {
		if(isCrafterUsed(test))
			return false;
		usedCrafters.add(test);
		return true;
	}
	
	public int getPromiseItemCount() {
		return promiseItemCount;
	}
	
	public int getMissingItemCount() {
		return request.stackSize - promiseItemCount;
	}
	
	public void addPromise(LogisticsPromise promise) {
		if(promise.item != request.getItem()) throw new IllegalArgumentException("wrong item");
		if(getMissingItemCount() == 0) throw new IllegalArgumentException("zero count");
		if(promise.numberOfItems > getMissingItemCount()) {
			int more = promise.numberOfItems - getMissingItemCount();
			promise.numberOfItems = getMissingItemCount();
			//Add Extra
			LogisticsExtraPromise extra = new LogisticsExtraPromise();
			extra.item = promise.item;
			extra.numberOfItems = more;
			extra.sender = promise.sender;
			extra.relayPoints = new LinkedList<IRelayItem>();
			extra.relayPoints.addAll(promise.relayPoints);
			extrapromises.add(extra);
		}
		if(promise.numberOfItems <= 0) throw new IllegalArgumentException("zero count ... again");
		promises.add(promise);
		promiseItemCount += promise.numberOfItems;
	}

	public boolean isDone() {
		return getMissingItemCount() <= 0;
	}

	public boolean isAllDone() {
		boolean result = getMissingItemCount() <= 0;
		for(RequestTreeNode node:subRequests) {
			result &= node.isAllDone();
		}
		return result;
	}

	public ItemIdentifierStack getStack() {
		return request;
	}

	public boolean remove(RequestTreeNode subNode) {
		return subRequests.remove(subNode);	
	}
}
