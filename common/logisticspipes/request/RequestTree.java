package logisticspipes.request;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
	
	public LinkedList<LogisticsExtraPromise> getExtrasFor(ItemIdentifier item) {
		LinkedList<LogisticsExtraPromise> extras = new LinkedList<LogisticsExtraPromise>();
		checkForExtras(item,this,extras);
		removeUsedExtras(item,this,extras);
		return extras;
	}

	private void checkForExtras(ItemIdentifier item, RequestTreeNode node, LinkedList<LogisticsExtraPromise> extras) {
		for(LogisticsExtraPromise extra:node.extrapromises) {
			if(extra.item == item) {
				extras.add(extra.copy());
			}
		}
		for(RequestTreeNode subNode:node.subRequests) {
			checkForExtras(item,subNode,extras);
		}
	}

	private void removeUsedExtras(ItemIdentifier item, RequestTreeNode node, LinkedList<LogisticsExtraPromise> extras) {
		for(LogisticsPromise promise:node.promises) {
			if(promise.item != item) continue;
			if(!(promise instanceof LogisticsExtraPromise)) continue;
			LogisticsExtraPromise epromise = (LogisticsExtraPromise)promise;
			if(epromise.provided) continue;
			int usedcount = epromise.numberOfItems;
			for(LogisticsExtraPromise extra : extras) {
				if(extra.sender == epromise.sender) {
					if(extra.numberOfItems >= usedcount) {
						extra.numberOfItems -= usedcount;
						usedcount = 0;
						break;
					} else {
						usedcount -= extra.numberOfItems;
						extra.numberOfItems = 0;
					}
				}
			}
		}
		for(RequestTreeNode subNode:node.subRequests) {
			removeUsedExtras(item,subNode,extras);
		}
	}

	public void fullFillAll() {
		fullFill(this);
	}
	
	private void fullFill(RequestTreeNode node) {
		for(LogisticsPromise promise:node.promises) {
			promise.sender.fullFill(promise, node.target);
		}
		for(LogisticsPromise promise:node.extrapromises) {
			if(promise.sender instanceof ICraftItems) {
				((ICraftItems)promise.sender).registerExtras(promise.numberOfItems);
			}
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
}
