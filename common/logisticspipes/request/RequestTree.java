package logisticspipes.request;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import logisticspipes.interfaces.routing.ILiquidProvider;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.utils.FinalPair;
import logisticspipes.utils.IHavePriority;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.ItemMessage;
import logisticspipes.utils.LiquidIdentifier;

public class RequestTree extends RequestTreeNode {
	
	private HashMap<FinalPair<IProvideItems,ItemIdentifier>,Integer> _promisetotals;

	public RequestTree(ItemIdentifierStack item, IRequestItems requester, RequestTree parent) {
		super(item, requester, parent);
	}
	
	private int getExistingPromisesFor(FinalPair key) {
		if(_promisetotals == null)
			_promisetotals = new HashMap<FinalPair<IProvideItems,ItemIdentifier>,Integer>();
		Integer n = _promisetotals.get(key);
		if(n == null) return 0;
		return n;
	}


	protected int getAllPromissesFor(IProvideItems provider, ItemIdentifier item) {
		FinalPair<IProvideItems,ItemIdentifier> key = new FinalPair<IProvideItems,ItemIdentifier>(provider, item);
		return getExistingPromisesFor(key);
	}
	
	protected LinkedList<LogisticsExtraPromise> getExtrasFor(ItemIdentifier item) {
		HashMap<IProvideItems,List<LogisticsExtraPromise>> extraMap = new HashMap<IProvideItems,List<LogisticsExtraPromise>>();
		checkForExtras(item,extraMap);
		removeUsedExtras(item,extraMap);
		LinkedList<LogisticsExtraPromise> extras = new LinkedList<LogisticsExtraPromise>();
		for(List<LogisticsExtraPromise> sublist:extraMap.values()) {
			extras.addAll(sublist);
		}
		return extras;
	}

	protected void fullFillAll() {
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

	protected void promiseAdded(LogisticsPromise promise) {
		FinalPair<IProvideItems,ItemIdentifier> key = new FinalPair<IProvideItems,ItemIdentifier>(promise.sender, promise.item);
		_promisetotals.put(key, getExistingPromisesFor(key) + promise.numberOfItems);
	}

	protected void promiseRemoved(LogisticsPromise promise) {
		FinalPair<IProvideItems,ItemIdentifier> key = new FinalPair<IProvideItems,ItemIdentifier>(promise.sender, promise.item);
		int r = getExistingPromisesFor(key) - promise.numberOfItems;
		if(r == 0) {
			_promisetotals.remove(key);
		} else {
			_promisetotals.put(key, r);
		}
	}

	public static class workWeightedSorter implements Comparator<ExitRoute> {

		public final double distanceWeight;
		public workWeightedSorter(double distanceWeight){this.distanceWeight=distanceWeight;}
		@Override
		public int compare(ExitRoute o1, ExitRoute o2) {
			double c=0;
			if(o1.destination.getPipe() instanceof IHavePriority) {
				if(o2.destination.getPipe() instanceof IHavePriority) {
					c = ((IHavePriority)o2.destination.getCachedPipe()).getPriority() - ((IHavePriority)o1.destination.getCachedPipe()).getPriority();
				} else {
					return -1;
				}
			} else {
				if(o2.destination.getPipe() instanceof IHavePriority) {
					return 1;
				}
			}
			if(c != 0) {
				return (int)c;
			}
			int flip = 1; // for enforcing consistancy of a<b vs b>a;
			if((o1.destination.getSimpleID() - o2.destination.getSimpleID()) < 0) {
				flip = -1;
				ExitRoute o_temp = o1;
				o1 = o2;
				o2 = o_temp;
				
			}
				
			c = o1.destination.getCachedPipe().getLoadFactor() - o2.destination.getCachedPipe().getLoadFactor();
			if(distanceWeight != 0) {
				c += (o1.distanceToDestination - o2.distanceToDestination) * distanceWeight;
			}
			if(c==0) {
				return -flip; // lowest ID first, of same distance.
			}
			if(c>0)
				return (int)(c+0.5)*flip; //round up
			else
				return (int)(c-0.5)*flip; //round down
		}
		
	}
	
	public static boolean request(List<ItemIdentifierStack> items, IRequestItems requester, RequestLog log) {
		LinkedList<ItemMessage> messages = new LinkedList<ItemMessage>();
		RequestTree tree = new RequestTree(new ItemIdentifierStack(ItemIdentifier.get(1,0,null), 0), requester, null);
		boolean isDone = true;
		for(ItemIdentifierStack stack:items) {
			messages.add(new ItemMessage(stack));
			RequestTree node = new RequestTree(stack, requester, tree);
			isDone = isDone && node.isDone();
		}
		if(isDone) {
			tree.fullFillAll();
			if(log != null) {
				log.handleSucessfullRequestOfList(messages);
			}
			return true;
		} else {
			if(log != null) {
				tree.logFailedRequestTree(log);
			}
			return false;
		}
	}
	
	public static int request(ItemIdentifierStack item, IRequestItems requester, RequestLog log, boolean acceptPartial, boolean simulateOnly, boolean logMissing, boolean logUsed) {
		RequestTree tree = new RequestTree(item, requester, null);
		if(!simulateOnly &&(tree.isDone() || ((tree.getPromiseItemCount() > 0) && acceptPartial))) {
			tree.fullFillAll();
			if(log != null) {
				log.handleSucessfullRequestOf(new ItemMessage(item));
			}
			return tree.getPromiseItemCount();
		} else {
			if(log != null) {
				if(!tree.isDone())
					tree.recurseFailedRequestTree();
				if(logMissing)
					tree.sendMissingMessage(log);
				if(logUsed)
					tree.sendUsedMessage(log);
					
			}
			return tree.getPromiseItemCount();
		}
	}

	public static int request(ItemIdentifierStack item,
			IRequestItems requester, RequestLog log) {
		return request( item, requester, log, false, false,true,false);
	}
	public static int requestPartial(ItemIdentifierStack item, IRequestItems requester) {
		return request( item, requester, null, true, false,true,false);
	}

	public static int simulate(ItemIdentifierStack item, IRequestItems requester, RequestLog log) {
		return request( item, requester, log, true, true, false, true);
	}	
	
	public static boolean requestLiquid(LiquidIdentifier liquid, int amount, IRequestLiquid pipe, List<ExitRoute> list, RequestLog log) {
		List<ILiquidProvider> providers = getLiquidProviders(list);
		LiquidRequest request = new LiquidRequest(liquid, amount);
		for(ILiquidProvider provider:providers) {
			provider.canProvide(request);
		}
		if(request.isAllDone()) {
			request.fullFill(pipe);
			if(log != null) {
				log.handleSucessfullRequestOf(new ItemMessage(request.getStack()));
			}
			return true;
		} else {
			if(log != null) {
				request.sendMissingMessage(log);
			}
			return false;
		}
	}

	private static List<ILiquidProvider> getLiquidProviders(List<ExitRoute> list) {
		List<ILiquidProvider> providers = new LinkedList<ILiquidProvider>();
		for(ExitRoute r : list) {
			CoreRoutedPipe pipe = r.destination.getPipe();
			if (pipe instanceof ILiquidProvider){
				providers.add((ILiquidProvider)pipe);
			}
		}
		return providers;
	}
}
