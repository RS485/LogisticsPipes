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
		int count = 0;
		for(LogisticsPromise promise:promises) {
			count += promise.numberOfItems;
		}
		return count;
	}
	
	public int getMissingItemCount() {
		return request.stackSize - getPromiseItemCount();
	}
	
	public boolean addPromise(LogisticsPromise promise) {
		if(promise.item != request.getItem()) return false;
		if(getMissingItemCount() == 0) return false;
		if(promise.numberOfItems > getMissingItemCount()) {
			int more = promise.numberOfItems - getMissingItemCount();
			promise.numberOfItems = getMissingItemCount();
			//Add Extra
			LogisticsExtraPromise extra = new LogisticsExtraPromise();
			extra.extraSource = this;
			extra.item = promise.item;
			extra.numberOfItems = more;
			extra.sender = promise.sender;
			extra.relayPoints = new LinkedList<IRelayItem>();
			extra.relayPoints.addAll(promise.relayPoints);
			if(promise instanceof LogisticsExtraPromise) {
				((LogisticsExtraPromise)promise).extraSource.addExtraPromise(extra);
			} else {
				extrapromises.add(extra);
			}
		}
		if(promise.numberOfItems > 0) {
			promises.add(promise);
			return true;
		}
		return false;
	}

	public void usePromise(LogisticsExtraPromise promise) {
		if (extrapromises.contains(promise)){
			extrapromises.remove(promise);
		}
	}

	public void addExtraPromise(LogisticsExtraPromise promise) {
		extrapromises.add(promise);
	}
	
	public boolean isDone() {
		boolean result = getMissingItemCount() <= 0;
		for(RequestTreeNode node:subRequests) {
			result &= node.isDone();
		}
		return result;
	}

	public ItemIdentifierStack getStack() {
		return request;
	}

	public void revertExtraUsage() {
		List<LogisticsPromise> toRemove = new ArrayList<LogisticsPromise>(promises.size());
		for(LogisticsPromise promise:promises) {
			if(promise instanceof LogisticsExtraPromise) {
				if(((LogisticsExtraPromise)promise).extraSource != this) {
					((LogisticsExtraPromise)promise).extraSource.addExtraPromise((LogisticsExtraPromise)promise);
					toRemove.add(promise);
				}
			}
		}
		for(LogisticsPromise promise:toRemove) {
			promises.remove(promise);
		}
		for(RequestTreeNode node:subRequests) {
			node.revertExtraUsage();
		}
	}
	
	public RequestTreeNode(RequestTreeNode other) {
		this.parentNode = other.parentNode;
		this.subRequests = new ArrayList<RequestTreeNode>(other.subRequests.size());
		for(RequestTreeNode subNode:other.subRequests) {
			this.subRequests.add(new RequestTreeNode(subNode));
		}
		
		this.promises = new ArrayList<LogisticsPromise>(other.promises.size());
		for(LogisticsPromise subpromises:other.promises) {
			this.promises.add(subpromises.copy());
		}

		this.extrapromises = new ArrayList<LogisticsExtraPromise>(other.extrapromises.size());
		for(LogisticsExtraPromise subpromises:other.extrapromises) {
			this.extrapromises.add(subpromises.copy());
		}

		this.usedCrafters = new TreeSet<CraftingTemplate>(other.usedCrafters);
		
		this.request = other.request;
		this.target = other.target;

	}

	public boolean remove(RequestTreeNode subNode) {
		return subRequests.remove(subNode);		
	}
}
