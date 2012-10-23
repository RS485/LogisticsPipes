package logisticspipes.request;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.utils.ItemIdentifierStack;

public class RequestTreeNode {

	public RequestTreeNode(ItemIdentifierStack item, IRequestItems requester) {
		this.request = item;
		this.target = requester;
	}

	
	protected final IRequestItems target;
	protected final ItemIdentifierStack request;
	protected List<RequestTreeNode> subRequests = new ArrayList<RequestTreeNode>();
	protected List<LogisticsPromise> promises = new ArrayList<LogisticsPromise>();
	protected List<LogisticsExtraPromise> extrapromises = new ArrayList<LogisticsExtraPromise>();
	
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
			extrapromises.add(extra);
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

	public void revertPromise(LogisticsExtraPromise promise) {
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
		List<LogisticsPromise> toRemove = new ArrayList<LogisticsPromise>();
		for(LogisticsPromise promise:promises) {
			if(promise instanceof LogisticsExtraPromise) {
				((LogisticsExtraPromise)promise).extraSource.revertPromise((LogisticsExtraPromise)promise);
				toRemove.add(promise);
			}
		}
		for(LogisticsPromise promise:toRemove) {
			promises.remove(promise);
		}
		for(RequestTreeNode node:subRequests) {
			node.revertExtraUsage();
		}
	}
	
	public RequestTreeNode copy() {
		RequestTreeNode result = new RequestTreeNode(request, target);
		for(RequestTreeNode subNode:subRequests) {
			result.subRequests.add(subNode.copy());
		}
		for(LogisticsPromise subpromises:promises) {
			result.promises.add(subpromises.copy());
		}
		for(LogisticsExtraPromise subpromises:extrapromises) {
			result.extrapromises.add(subpromises.copy());
		}
		return result;
	}
}
