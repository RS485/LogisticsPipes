package logisticspipes.request;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.routing.IRequestItems;
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
	protected List<LogisticsPromise> extrapromises = new ArrayList<LogisticsPromise>();
	
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
		if(promise.numberOfItems > getMissingItemCount()) {
			int more = promise.numberOfItems - getMissingItemCount();
			promise.numberOfItems = getMissingItemCount();
			//Add Extra
			LogisticsPromise extra = new LogisticsPromise();
			extra.extra = true;
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

	public void usePromise(LogisticsPromise promise) {
		if (extrapromises.contains(promise)){
			extrapromises.remove(promise);
		}
	}
	
	public boolean isDone() {
		return getMissingItemCount() <= 0;
	}

	public ItemIdentifierStack getStack() {
		return request;
	}

	public RequestTreeNode copy() {
		RequestTreeNode result = new RequestTreeNode(request, target);
		for(RequestTreeNode subNode:subRequests) {
			result.subRequests.add(subNode.copy());
		}
		for(LogisticsPromise subpromises:promises) {
			result.promises.add(subpromises.copy());
		}
		for(LogisticsPromise subpromises:extrapromises) {
			result.extrapromises.add(subpromises.copy());
		}
		return result;
	}
}
