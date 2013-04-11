package logisticspipes.request;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.utils.FinalPair;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.ItemMessage;

public class RequestTree extends RequestTreeNode {
	
	private final HashMap<FinalPair<IProvideItems,ItemIdentifier>,Integer> _promisetotals = new HashMap<FinalPair<IProvideItems,ItemIdentifier>,Integer>();

	public RequestTree(ItemIdentifierStack item, IRequestItems requester, RequestTree parent) {
		super(item, requester, parent);
	}

	public int getAllPromissesFor(IProvideItems provider, ItemIdentifier item) {
		FinalPair<IProvideItems,ItemIdentifier> key = new FinalPair<IProvideItems,ItemIdentifier>(provider, item);
		Integer n = _promisetotals.get(key);
		if(n == null) return 0;
		return n;
	}
	
	public LinkedList<LogisticsExtraPromise> getExtrasFor(ItemIdentifier item) {
		HashMap<IProvideItems,List<LogisticsExtraPromise>> extraMap = new HashMap<IProvideItems,List<LogisticsExtraPromise>>();
		checkForExtras(item,extraMap);
		removeUsedExtras(item,extraMap);
		LinkedList<LogisticsExtraPromise> extras = new LinkedList<LogisticsExtraPromise>();
		for(List<LogisticsExtraPromise> sublist:extraMap.values()) {
			extras.addAll(sublist);
		}
		return extras;
	}

	public void fullFillAll() {
		fullFill();
	}
	
	public void sendMissingMessage(RequestLog log) {
		LinkedList<ItemMessage> missing = new LinkedList<ItemMessage>();
		sendMissingMessage(missing);
		ItemMessage.compress(missing);
		log.handleMissingItems(missing);
	}

	public void sendUsedMessage(RequestLog log) {
		LinkedList<ItemMessage> used = new LinkedList<ItemMessage>();
		LinkedList<ItemMessage> missing = new LinkedList<ItemMessage>();
		sendUsedMessage(used, missing);
		ItemMessage.compress(used);
		ItemMessage.compress(missing);
		log.handleSucessfullRequestOfList(used);
		log.handleMissingItems(missing);
	}

	public void promiseAdded(LogisticsPromise promise) {
		FinalPair<IProvideItems,ItemIdentifier> key = new FinalPair<IProvideItems,ItemIdentifier>(promise.sender, promise.item);
		Integer n = _promisetotals.get(key);
		if(n == null) {
			_promisetotals.put(key, promise.numberOfItems);
		} else {
			_promisetotals.put(key, n + promise.numberOfItems);
		}
	}

	public void promiseRemoved(LogisticsPromise promise) {
		FinalPair<IProvideItems,ItemIdentifier> key = new FinalPair<IProvideItems,ItemIdentifier>(promise.sender, promise.item);
		int r = _promisetotals.get(key) - promise.numberOfItems;
		if(r == 0) {
			_promisetotals.remove(key);
		} else {
			_promisetotals.put(key, r);
		}
	}
}
