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
	
	public RequestTree(ItemIdentifierStack item, IRequestItems requester) {
		super(item, requester);
	}

	public Map<ItemIdentifier, Integer> getAllPromissesFor(IProvideItems provider) {
		Map<ItemIdentifier, Integer> result = new HashMap<ItemIdentifier, Integer>();
		chechSubPromisses(provider,this,result);
		return result;
	}
	
	private void chechSubPromisses(IProvideItems provider, RequestTreeNode node, Map<ItemIdentifier, Integer> result) {
		for(LogisticsPromise promise: node.promises) {
			if(promise.sender == provider) {
				if(result.containsKey(promise.item)) {
					result.put(promise.item, promise.numberOfItems + result.get(promise.item));
				} else {
					result.put(promise.item, promise.numberOfItems);
				}
			}
		}
		for(RequestTreeNode subNode:node.subRequests) {
			chechSubPromisses(provider,subNode,result);
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
	
	public LinkedHashMap<LogisticsPromise,RequestTreeNode> getExtrasFor(ItemIdentifier item) {
		LinkedHashMap<LogisticsPromise,RequestTreeNode> extras = new LinkedHashMap<LogisticsPromise,RequestTreeNode>();
		checkForExtras(item,this,extras);
		return extras;
	}

	private void checkForExtras(ItemIdentifier item, RequestTreeNode node, LinkedHashMap<LogisticsPromise,RequestTreeNode> extras) {
		for(LogisticsPromise extra:node.extrapromises) {
			if(extra.item == item) {
				extras.put(extra, node);
			}
		}
		for(RequestTreeNode subNode:node.subRequests) {
			checkForExtras(item,subNode,extras);
		}
	}
	
	public RequestTree copy() {
		RequestTree result = new RequestTree(request, target);
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
		if(!node.isDone()) {
			ItemIdentifierStack stack = node.getStack().clone();
			stack.stackSize = node.getMissingItemCount();
			missing.add(new ItemMessage(stack));
		}
		for(RequestTreeNode subNode:node.subRequests) {
			sendMissingMessage(missing, subNode);
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
