package logisticspipes.request;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IFluidProvider;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.FluidLogisticsPromise;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.routing.order.LinkedLogisticsOrderList;
import logisticspipes.utils.FinalPair;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.IHavePriority;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

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

	public RequestTree(ItemIdentifierStack item, IRequestItems requester, RequestTree parent, EnumSet<ActiveRequestType> requestFlags, IAdditionalTargetInformation info) {
		super(item, requester, parent, requestFlags, info);
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

	protected LinkedLogisticsOrderList fullFillAll() {
		return fullFill();
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
		log.handleSucessfullRequestOfList(used, new LinkedLogisticsOrderList());
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
			int c=0;
			if(o1.destination.getPipe() instanceof IHavePriority) {
				if(o2.destination.getPipe() instanceof IHavePriority) {
					c = ((IHavePriority)o2.destination.getCachedPipe()).getPriority() - ((IHavePriority)o1.destination.getCachedPipe()).getPriority();
					if(c != 0) {
						return c;
					}
				} else {
					return -1;
				}
			} else {
				if(o2.destination.getPipe() instanceof IHavePriority) {
					return 1;
				}
			}

			//GetLoadFactor*64 should be an integer anyway.
			c = (int)Math.floor(o1.destination.getCachedPipe().getLoadFactor()*64) - (int)Math.floor(o2.destination.getCachedPipe().getLoadFactor()*64);
			if(distanceWeight != 0) {
				c += (int)(Math.floor(o1.distanceToDestination*64) - (int)Math.floor(o2.distanceToDestination*64)) * distanceWeight;
			}
			/*
			if (c < 0) 
			{
			   return -Math.ceil(-c);
			}
			else
			{
			   return Math.ceil(c);
			}*/
			return c;
		}
		
	}
	
	public static boolean request(List<ItemIdentifierStack> items, IRequestItems requester, RequestLog log, EnumSet<ActiveRequestType> requestFlags, IAdditionalTargetInformation info) {
		Map<ItemIdentifier,Integer> messages = new HashMap<ItemIdentifier,Integer>();
		RequestTree tree = new RequestTree(new ItemIdentifierStack(ItemIdentifier.get(1,0,null), 0), requester, null, requestFlags, info);
		boolean isDone = true;
		for(ItemIdentifierStack stack:items) {
			ItemIdentifier item = stack.getItem();
			Integer count = messages.get(item);
			if(count == null)
				count = 0;
			count += stack.getStackSize();
			messages.put(item, count);
			RequestTree node = new RequestTree(stack, requester, tree, requestFlags, info);
			isDone = isDone && node.isDone();
		}
		if(isDone) {
			LinkedLogisticsOrderList list = tree.fullFillAll();
			if(log != null) {
				log.handleSucessfullRequestOfList(messages, list);
			}
			return true;
		} else {
			if(log != null) {
				tree.logFailedRequestTree(log);
			}
			return false;
		}
	}
	
	public static int request(ItemIdentifierStack item, IRequestItems requester, RequestLog log, boolean acceptPartial, boolean simulateOnly, boolean logMissing, boolean logUsed, EnumSet<ActiveRequestType> requestFlags, IAdditionalTargetInformation info) {
		RequestTree tree = new RequestTree(item, requester, null, requestFlags, info);
		if(!simulateOnly &&(tree.isDone() || ((tree.getPromiseItemCount() > 0) && acceptPartial))) {
			LinkedLogisticsOrderList list = tree.fullFillAll();
			if(log != null) {
				log.handleSucessfullRequestOf(item.getItem(), item.getStackSize(), list);
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

	public static boolean request(ItemIdentifierStack item, IRequestItems requester, RequestLog log, IAdditionalTargetInformation info) {
		return request( item, requester, log, false, false,true,false,defaultRequestFlags, info) == item.getStackSize();
	}
	
	public static int requestPartial(ItemIdentifierStack item, IRequestItems requester, IAdditionalTargetInformation info) {
		return request( item, requester, null, true, false,true,false,defaultRequestFlags, info);
	}

	public static int simulate(ItemIdentifierStack item, IRequestItems requester, RequestLog log) {
		return request( item, requester, log, true, true, false, true,defaultRequestFlags, null);
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
			//TODO add control for Liquid
			request.fullFill();
			if(log != null) {
				log.handleSucessfullRequestOf(request.getFluid().getItemIdentifier(), request.getAmount(), null);
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
