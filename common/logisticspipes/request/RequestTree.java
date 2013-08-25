package logisticspipes.request;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import logisticspipes.interfaces.routing.IFluidProvider;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.FluidLogisticsPromise;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.utils.FinalPair;
import logisticspipes.utils.IHavePriority;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.FluidIdentifier;

public class RequestTree extends RequestTreeNode {

	public static enum ActiveRequestType {
		Provide,
		Craft,
		AcceptPartial, 
		SimulateOnly, 
		LogMissing, 
		LogUsed	
	}
	
	public static final EnumSet<ActiveRequestType> defaultRequestFlags=EnumSet.of(ActiveRequestType.Provide,ActiveRequestType.Craft);
	private HashMap<FinalPair<IProvideItems,ItemIdentifier>,Integer> _promisetotals;
	private HashMap<FinalPair<IFluidProvider,FluidIdentifier>,Integer> _promisetotalsliquid;

	public RequestTree(ItemIdentifierStack item, IRequestItems requester, RequestTree parent, EnumSet<ActiveRequestType> requestFlags) {
		super(item, requester, parent, requestFlags);
	}
	
	private int getExistingPromisesFor(FinalPair<IProvideItems, ItemIdentifier> key) {
		if(_promisetotals == null)
			_promisetotals = new HashMap<FinalPair<IProvideItems,ItemIdentifier>,Integer>();
		Integer n = _promisetotals.get(key);
		if(n == null) return 0;
		return n;
	}

	private int getExistingFluidPromisesFor(FinalPair<IFluidProvider, FluidIdentifier> key) {
		if(_promisetotalsliquid == null)
			_promisetotalsliquid = new HashMap<FinalPair<IFluidProvider,FluidIdentifier>,Integer>();
		Integer n = _promisetotalsliquid.get(key);
		if(n == null) return 0;
		return n;
	}

	protected int getAllPromissesFor(IProvideItems provider, ItemIdentifier item) {
		FinalPair<IProvideItems,ItemIdentifier> key = new FinalPair<IProvideItems,ItemIdentifier>(provider, item);
		return getExistingPromisesFor(key);
	}
	
	protected int getAllPromissesFor(IFluidProvider provider, FluidIdentifier liquid) {
		FinalPair<IFluidProvider, FluidIdentifier> key = new FinalPair<IFluidProvider,FluidIdentifier>(provider, liquid);
		return getExistingFluidPromisesFor(key);
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
		Map<ItemIdentifier,Integer> missing = new HashMap<ItemIdentifier,Integer>();
		buildMissingMap(missing);
		log.handleMissingItems(missing);
	}

	public void sendUsedMessage(RequestLog log) {
		Map<ItemIdentifier,Integer> used = new HashMap<ItemIdentifier,Integer>();
		Map<ItemIdentifier,Integer> missing = new HashMap<ItemIdentifier,Integer>();
		buildUsedMap(used, missing);
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

	protected void promiseAdded(FluidLogisticsPromise promise) {
		FinalPair<IFluidProvider, FluidIdentifier> key = new FinalPair<IFluidProvider,FluidIdentifier>(promise.sender, promise.liquid);
		_promisetotalsliquid.put(key, getExistingFluidPromisesFor(key) + promise.amount);
	}

	protected void promiseRemoved(FluidLogisticsPromise promise) {
		FinalPair<IFluidProvider,FluidIdentifier> key = new FinalPair<IFluidProvider,FluidIdentifier>(promise.sender, promise.liquid);
		int r = getExistingFluidPromisesFor(key) - promise.amount;
		if(r == 0) {
			_promisetotalsliquid.remove(key);
		} else {
			_promisetotalsliquid.put(key, r);
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
	
	public static boolean request(List<ItemIdentifierStack> items, IRequestItems requester, RequestLog log, EnumSet<ActiveRequestType> requestFlags) {
		Map<ItemIdentifier,Integer> messages = new HashMap<ItemIdentifier,Integer>();
		RequestTree tree = new RequestTree(new ItemIdentifierStack(ItemIdentifier.get(1,0,null), 0), requester, null, requestFlags);
		boolean isDone = true;
		for(ItemIdentifierStack stack:items) {
			ItemIdentifier item = stack.getItem();
			Integer count = messages.get(item);
			if(count == null)
				count = 0;
			count += stack.stackSize;
			messages.put(item, count);
			RequestTree node = new RequestTree(stack, requester, tree, requestFlags);
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
	
	public static int request(ItemIdentifierStack item, IRequestItems requester, RequestLog log, boolean acceptPartial, boolean simulateOnly, boolean logMissing, boolean logUsed, EnumSet<ActiveRequestType> requestFlags) {
		RequestTree tree = new RequestTree(item, requester, null, requestFlags);
		if(!simulateOnly &&(tree.isDone() || ((tree.getPromiseItemCount() > 0) && acceptPartial))) {
			tree.fullFillAll();
			if(log != null) {
				log.handleSucessfullRequestOf(item.getItem(), item.stackSize);
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

	public static boolean request(ItemIdentifierStack item, IRequestItems requester, RequestLog log) {
		return request( item, requester, log, false, false,true,false,defaultRequestFlags) == item.stackSize;
	}
	
	public static int requestPartial(ItemIdentifierStack item, IRequestItems requester) {
		return request( item, requester, null, true, false,true,false,defaultRequestFlags);
	}

	public static int simulate(ItemIdentifierStack item, IRequestItems requester, RequestLog log) {
		return request( item, requester, log, true, true, false, true,defaultRequestFlags);
	}
	
	public static int requestFluidPartial(FluidIdentifier liquid, int amount, IRequestFluid pipe, RequestLog log) {
		return requestFluid(liquid, amount, pipe, log, true);
	}

	public static boolean requestFluid(FluidIdentifier liquid, int amount, IRequestFluid pipe, RequestLog log) {
		return requestFluid(liquid, amount, pipe, log, false)  == amount;
	}
	
	private static int requestFluid(FluidIdentifier liquid, int amount, IRequestFluid pipe, RequestLog log, boolean acceptPartial) {
		FluidRequestTreeNode request = new FluidRequestTreeNode(liquid, amount, pipe, null);
		if(request.isDone() || acceptPartial) {
			request.fullFill();
			if(log != null) {
				log.handleSucessfullRequestOf(request.getFluid().getItemIdentifier(), request.getAmount());
			}
			return request.getPromiseFluidAmount();
		} else {
			if(log != null) {
				request.sendMissingMessage(log);
			}
			return request.getPromiseFluidAmount();
		}
	}
}
