package logisticspipes.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.utils.FinalPair;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.ItemMessage;

public class RequestTreeNode {

	public RequestTreeNode(ItemIdentifierStack item, IRequestItems requester, RequestTreeNode parentNode) {
		this.request = item;
		this.target = requester;
		this.parentNode=parentNode;
		if(parentNode!=null) {
			parentNode.subRequests.add(this);
			this.root = parentNode.root;
		} else {
			this.root = (RequestTree)this;
		}
	}

	
	protected final IRequestItems target;
	protected final ItemIdentifierStack request;
	protected final RequestTreeNode parentNode;
	protected final RequestTree root;
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
		root.promiseAdded(promise);
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

	public void remove(List<RequestTreeNode> subNodes) {
		subRequests.removeAll(subNodes);
		for(RequestTreeNode subnode:subNodes) {
			subnode.removeSubPromisses();
		}
	}


	/* RequestTree helpers */
	protected void removeSubPromisses() {
		for(LogisticsPromise promise:promises) {
			root.promiseRemoved(promise);
		}
		for(RequestTreeNode subNode:subRequests) {
			subNode.removeSubPromisses();
		}
	}

	protected void checkForExtras(ItemIdentifier item, HashMap<IProvideItems,List<LogisticsExtraPromise>> extraMap) {
		for(LogisticsExtraPromise extra:extrapromises) {
			if(extra.item == item) {
				List<LogisticsExtraPromise> extras = extraMap.get(extra.sender);
				if(extras == null) {
					extras = new LinkedList<LogisticsExtraPromise>();
					extraMap.put(extra.sender, extras);
				}
				extras.add(extra.copy());
			}
		}
		for(RequestTreeNode subNode:subRequests) {
			subNode.checkForExtras(item, extraMap);
		}
	}

	protected void removeUsedExtras(ItemIdentifier item, HashMap<IProvideItems,List<LogisticsExtraPromise>> extraMap) {
		for(LogisticsPromise promise:promises) {
			if(promise.item != item) continue;
			if(!(promise instanceof LogisticsExtraPromise)) continue;
			LogisticsExtraPromise epromise = (LogisticsExtraPromise)promise;
			if(epromise.provided) continue;
			int usedcount = epromise.numberOfItems;
			List<LogisticsExtraPromise> extras = extraMap.get(epromise.sender);
			if(extras == null) continue;
			for(Iterator<LogisticsExtraPromise> it = extras.iterator(); it.hasNext();) {
				LogisticsExtraPromise extra = it.next();
				if(extra.numberOfItems >= usedcount) {
					extra.numberOfItems -= usedcount;
					usedcount = 0;
					break;
				} else {
					usedcount -= extra.numberOfItems;
					it.remove();
				}
			}
		}
		for(RequestTreeNode subNode:subRequests) {
			subNode.removeUsedExtras(item,extraMap);
		}
	}

	protected void fullFill() {
		for(RequestTreeNode subNode:subRequests) {
			subNode.fullFill();
		}
		for(LogisticsPromise promise:promises) {
			promise.sender.fullFill(promise, target);
		}
		for(LogisticsPromise promise:extrapromises) {
			if(promise.sender instanceof ICraftItems) {
				((ICraftItems)promise.sender).registerExtras(promise.numberOfItems);
			}
		}
	}

	protected void sendMissingMessage(LinkedList<ItemMessage> missing) {
		if(getMissingItemCount() != 0) {
			ItemIdentifierStack stack = getStack().clone();
			stack.stackSize = getMissingItemCount();
			missing.add(new ItemMessage(stack));
		}
		for(RequestTreeNode subNode:subRequests) {
			subNode.sendMissingMessage(missing);
		}
	}

	protected void sendUsedMessage(LinkedList<ItemMessage> used, LinkedList<ItemMessage> missing) {
		int usedcount = 0;
		for(LogisticsPromise promise:promises) {
			if(promise.sender instanceof IProvideItems && !(promise.sender instanceof ICraftItems)) {
				usedcount += promise.numberOfItems;
			}
		}
		if(usedcount != 0) {
			ItemIdentifierStack stack = getStack().clone();
			stack.stackSize = usedcount;
			used.add(new ItemMessage(stack));
		}
		if(getMissingItemCount() != 0) {
			ItemIdentifierStack stack = getStack().clone();
			stack.stackSize = getMissingItemCount();
			missing.add(new ItemMessage(stack));
		}
		for(RequestTreeNode subNode:subRequests) {
			subNode.sendUsedMessage(used, missing);
		}
	}
}
