package logisticspipes.request;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.ItemMessage;

public class RequestTree extends RequestTreeNode {
	
	public RequestTree(ItemIdentifierStack item, IRequestItems requester, RequestTree parent) {
		super(item, requester, parent);
	}
	public RequestTree(RequestTreeNode other) {
		super(other);
	}

	public Map<ItemIdentifier, Integer> getAllPromissesFor(IProvideItems provider) {
		Map<ItemIdentifier, Integer> result = new HashMap<ItemIdentifier, Integer>();
		checkSubPromisses(provider,this,result);
		return result;
	}
	
	private void checkSubPromisses(IProvideItems provider, RequestTreeNode node, Map<ItemIdentifier, Integer> result) {
		for(LogisticsPromise promise: node.promises) {
			if(promise.sender == provider) {
				Integer count=result.get(promise.item);
				if(count == null) {
					result.put(promise.item, promise.numberOfItems);
				} else {
					result.put(promise.item, promise.numberOfItems + count);
				}
			}
		}
		for(RequestTreeNode subNode:node.subRequests) {
			checkSubPromisses(provider,subNode,result);
		}
	}
	
	public boolean isAllDone() {
		return checkSubDone(this);
	}
	
	private boolean checkSubDone(RequestTreeNode node) {
		boolean done = true;
		done &= node.isDone();
		for(RequestTreeNode subNode:node.subRequests) {
			done &= checkSubDone(subNode);
		}
		return done;
	}
	
	public LinkedHashMap<LogisticsExtraPromise,RequestTreeNode> getExtrasFor(ItemIdentifier item) {
		LinkedHashMap<LogisticsExtraPromise,RequestTreeNode> extras = new LinkedHashMap<LogisticsExtraPromise,RequestTreeNode>();
		checkForExtras(item,this,extras);
		return extras;
	}

	private void checkForExtras(ItemIdentifier item, RequestTreeNode node, LinkedHashMap<LogisticsExtraPromise,RequestTreeNode> extras) {
		for(LogisticsExtraPromise extra:node.extrapromises) {
			if(extra.item == item) {
				extras.put(extra, node);
			}
		}
		for(RequestTreeNode subNode:node.subRequests) {
			checkForExtras(item,subNode,extras);
		}
	}
	
	public void fullFillAll() {
		fullFill(this);
	}
	
	private void fullFill(RequestTreeNode node) {
		for(LogisticsPromise promise:node.promises) {
			promise.sender.fullFill(promise, node.target);
		}
		for(RequestTreeNode subNode:node.subRequests) {
			fullFill(subNode);
		}
	}

	public void sendMissingMessage(RequestLog log) {
		LinkedList<ItemMessage> missing = new LinkedList<ItemMessage>();
		sendMissingMessage(missing, this);
		ItemMessage.compress(missing);
		log.handleMissingItems(missing);
	}

	private void sendMissingMessage(LinkedList<ItemMessage> missing, RequestTreeNode node) {
		if(node.getMissingItemCount() != 0) {
			ItemIdentifierStack stack = node.getStack().clone();
			stack.stackSize = node.getMissingItemCount();
			missing.add(new ItemMessage(stack));
		}
		for(RequestTreeNode subNode:node.subRequests) {
			sendMissingMessage(missing, subNode);
		}
	}
	
	public void sendUsedMessage(RequestLog log) {
		LinkedList<ItemMessage> used = new LinkedList<ItemMessage>();
		LinkedList<ItemMessage> missing = new LinkedList<ItemMessage>();
		sendUsedMessage(used, missing, this);
		ItemMessage.compress(used);
		ItemMessage.compress(missing);
		log.handleSucessfullRequestOfList(used);
		log.handleMissingItems(missing);
	}

	private void sendUsedMessage(LinkedList<ItemMessage> used, LinkedList<ItemMessage> missing, RequestTreeNode node) {
		int usedcount = 0;
		for(LogisticsPromise promise:node.promises) {
			if(promise.sender instanceof IProvideItems && !(promise.sender instanceof ICraftItems)) {
				usedcount += promise.numberOfItems;
			}
		}
		if(usedcount != 0) {
			ItemIdentifierStack stack = node.getStack().clone();
			stack.stackSize = usedcount;
			used.add(new ItemMessage(stack));
		}
		if(node.getMissingItemCount() != 0) {
			ItemIdentifierStack stack = node.getStack().clone();
			stack.stackSize = node.getMissingItemCount();
			missing.add(new ItemMessage(stack));
		}
		for(RequestTreeNode subNode:node.subRequests) {
			sendUsedMessage(used, missing, subNode);
		}
	}

	public void registerExtras() {
		registerExtras(this);
	}
	
	private void registerExtras(RequestTreeNode node) {
		for(LogisticsPromise promise:node.extrapromises) {
			if(promise.sender instanceof ICraftItems) {
				((ICraftItems)promise.sender).registerExtras(promise.numberOfItems);
			}
		}
		for(RequestTreeNode subNode:node.subRequests) {
			registerExtras(subNode);
		}
	}
}
