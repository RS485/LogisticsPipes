package logisticspipes.request;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.TreeSet;

import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IFilteringRouter;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTree.ActiveRequestType;
import logisticspipes.request.RequestTree.workWeightedSorter;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.ServerRouter;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.Pair;
import logisticspipes.utils.Pair3;

public class RequestTreeNode {

	protected RequestTreeNode(ItemIdentifierStack item, IRequestItems requester, RequestTreeNode parentNode, EnumSet<ActiveRequestType> requestFlags) {
		this(null,item,requester,parentNode,requestFlags);
	}
	private RequestTreeNode(CraftingTemplate template, ItemIdentifierStack item, IRequestItems requester, RequestTreeNode parentNode, EnumSet<ActiveRequestType> requestFlags) {
		this.request = item;
		this.target = requester;
		this.parentNode=parentNode;
//		this.requestFlags=requestFlags;
		if(parentNode!=null) {
			parentNode.subRequests.add(this);
			this.root = parentNode.root;
		} else {
			this.root = (RequestTree)this;
		}
		if(template!=null) {
			this.declareCrafterUsed(template);
		}
		
		if(requestFlags.contains(ActiveRequestType.Provide) && checkProvider()){
			return;
		}

		if(requestFlags.contains(ActiveRequestType.Craft) && checkExtras()) {
			return;// crafting was able to complete
		}
		
		if(requestFlags.contains(ActiveRequestType.Craft) && checkCrafting()) {
			return;// crafting was able to complete
		}
		
		// crafting is not done!
	}

//	private final EnumSet<ActiveRequestType> requestFlags;
	private final IRequestItems target;
	private final ItemIdentifierStack request;
	private final RequestTreeNode parentNode;
	protected final RequestTree root;
	private List<RequestTreeNode> subRequests = new ArrayList<RequestTreeNode>();
	List<FluidRequestTreeNode> liquidSubRequests = new ArrayList<FluidRequestTreeNode>();
	private List<LogisticsPromise> promises = new ArrayList<LogisticsPromise>();
	private List<LogisticsExtraPromise> extrapromises = new ArrayList<LogisticsExtraPromise>();
	private List<LogisticsExtraPromise> byproducts = new ArrayList<LogisticsExtraPromise>();
	private SortedSet<CraftingTemplate> usedCrafters= new TreeSet<CraftingTemplate>();
	private CraftingTemplate lastCrafterTried = null;
	
	private int promiseItemCount = 0;

	private boolean isCrafterUsed(CraftingTemplate test) {
		if(!usedCrafters.isEmpty() && usedCrafters.contains(test))
			return true;
		if(parentNode==null)
			return false;
		return parentNode.isCrafterUsed(test);
	}
	
	// returns false if the crafter was already on the list.
	private boolean declareCrafterUsed(CraftingTemplate test) {
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
		if(getMissingItemCount() == 0) throw new IllegalArgumentException("zero count needed, promises not needed.");
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

	public ItemIdentifier getStackItem() {
		return request.getItem();
	}

	protected void remove(RequestTreeNode subNode) {
		subRequests.remove(subNode);
		subNode.removeSubPromisses();
	}
	
	protected void remove(FluidRequestTreeNode subNode) {
		liquidSubRequests.remove(subNode);
		subNode.removeSubPromisses();
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
				((ICraftItems)promise.sender).registerExtras(promise);
			}
		}
		for(LogisticsPromise promise:byproducts) {
			if(promise.sender instanceof ICraftItems) {
				((ICraftItems)promise.sender).registerExtras(promise);
			}
		}
		for(FluidRequestTreeNode subNode:liquidSubRequests) {
			subNode.fullFill();
		}
	}

	protected void buildMissingMap(Map<ItemIdentifier,Integer> missing) {
		if(getMissingItemCount() != 0) {
			ItemIdentifier item = request.getItem();
			Integer count = missing.get(item);
			if(count == null)
				count = 0;
			count += getMissingItemCount();
			missing.put(item, count);
		}
		for(RequestTreeNode subNode:subRequests) {
			subNode.buildMissingMap(missing);
		}
		for(FluidRequestTreeNode subNode:liquidSubRequests) {
			subNode.buildMissingMap(missing);
		}
	}

	protected void buildUsedMap(Map<ItemIdentifier,Integer> used, Map<ItemIdentifier,Integer> missing) {
		int usedcount = 0;
		for(LogisticsPromise promise:promises) {
			if(promise.sender instanceof IProvideItems && !(promise.sender instanceof ICraftItems)) {
				usedcount += promise.numberOfItems;
			}
		}
		if(usedcount != 0) {
			ItemIdentifier item = request.getItem();
			Integer count = used.get(item);
			if(count == null)
				count = 0;
			count += usedcount;
			used.put(item, count);
		}
		if(getMissingItemCount() != 0) {
			ItemIdentifier item = request.getItem();
			Integer count = missing.get(item);
			if(count == null)
				count = 0;
			count += getMissingItemCount();
			missing.put(item, count);
		}
		for(RequestTreeNode subNode:subRequests) {
			subNode.buildUsedMap(used, missing);
		}
		for(FluidRequestTreeNode subNode:liquidSubRequests) {
			subNode.buildUsedMap(used, missing);
		}
	}
	
	private boolean checkProvider() {
		
		CoreRoutedPipe thisPipe = this.target.getRouter().getCachedPipe();
		for(Pair<IProvideItems, List<IFilter>> provider : getProviders(this.target.getRouter(), this.getStackItem(), new BitSet(ServerRouter.getBiggestSimpleID()), new LinkedList<IFilter>())) {
			if(this.isDone()) {
				break;
			}
			if(!thisPipe.sharesInventoryWith(provider.getValue1().getRouter().getPipe())) {
				provider.getValue1().canProvide(this, root.getAllPromissesFor(provider.getValue1(), this.getStackItem()), provider.getValue2());
			}
		}
		return this.isDone();
	}

	private static List<Pair<IProvideItems,List<IFilter>>> getProviders(IRouter destination, ItemIdentifier item, BitSet layer, List<IFilter> filters) {

		// get all the routers
		BitSet routersIndex = ServerRouter.getRoutersInterestedIn(item);
		List<ExitRoute> validSources = new ArrayList<ExitRoute>(); // get the routing table 
		for (int i = routersIndex.nextSetBit(0); i >= 0; i = routersIndex.nextSetBit(i+1)) {
			IRouter r = SimpleServiceLocator.routerManager.getRouterUnsafe(i,false);

			if(!r.isValidCache()) continue; //Skip Routers without a valid pipe

			ExitRoute e = destination.getDistanceTo(r);
			if (e!=null) validSources.add(e);
		}
		// closer providers are good
		Collections.sort(validSources, new workWeightedSorter(1.0));
		
		List<Pair<IProvideItems,List<IFilter>>> providers = new LinkedList<Pair<IProvideItems,List<IFilter>>>();
		List<ExitRoute> firewalls = new LinkedList<ExitRoute>();
		BitSet used = (BitSet) layer.clone();
		for(ExitRoute r : validSources) {
			if(r.containsFlag(PipeRoutingConnectionType.canRequestFrom) && !used.get(r.destination.getSimpleID())) {
				CoreRoutedPipe pipe = r.destination.getPipe();
				if (pipe instanceof IProvideItems) {
					List<IFilter> list = new LinkedList<IFilter>();
					list.addAll(filters);
					providers.add(new Pair<IProvideItems,List<IFilter>>((IProvideItems)pipe, list));
					used.set(r.root.getSimpleID());
				}
				if(r.destination instanceof IFilteringRouter) {
					firewalls.add(r);
					used.set(r.destination.getSimpleID());
				}
			}
		}
		for(ExitRoute r:firewalls) {
			IFilter filter = ((IFilteringRouter)r.destination).getFilter();
			filters.add(filter);
			for(ExitRoute router:((IFilteringRouter)r.destination).getRouters()) {
				providers.addAll(getProviders(router.destination, item, used, filters));
			}
			filters.remove(filter);
		}
		return providers;
	}
	
	private boolean checkExtras() {
		LinkedList<LogisticsExtraPromise> map = root.getExtrasFor(this.getStackItem());
		for (LogisticsExtraPromise extraPromise : map){
			if(this.isDone()) {
				break;
			}
			if(extraPromise.numberOfItems == 0)
				continue;
			boolean valid = false;
			ExitRoute source = extraPromise.sender.getRouter().getRouteTable().get(this.target.getRouter().getSimpleID());
			if(source != null && source.containsFlag(PipeRoutingConnectionType.canRouteTo)) {
				for(ExitRoute node:this.target.getRouter().getIRoutersByCost()) {
					if(node.destination == extraPromise.sender.getRouter()) {
						if(node.containsFlag(PipeRoutingConnectionType.canRequestFrom)) {
							valid = true;
						}
					}
				}
			}
			if(valid) {
				extraPromise.numberOfItems = Math.min(extraPromise.numberOfItems, this.getMissingItemCount());
				this.addPromise(extraPromise);
			}
		}
		return isDone();
	}
	
	private boolean checkCrafting() {
		
		// get all the routers
		BitSet routersIndex = ServerRouter.getRoutersInterestedIn(this.getStackItem());
		List<ExitRoute> validSources = new ArrayList<ExitRoute>(); // get the routing table 
		for (int i = routersIndex.nextSetBit(0); i >= 0; i = routersIndex.nextSetBit(i+1)) {
			IRouter r = SimpleServiceLocator.routerManager.getRouterUnsafe(i,false);

			if(!r.isValidCache()) continue; //Skip Routers without a valid pipe

			ExitRoute e = this.target.getRouter().getDistanceTo(r);
			if (e!=null) validSources.add(e);
		}
		workWeightedSorter wSorter = new workWeightedSorter(0); // distance doesn't matter, because ingredients have to be delivered to the crafter, and we can't tell how long that will take.
		Collections.sort(validSources, wSorter);
		
		List<Pair<CraftingTemplate, List<IFilter>>> allCraftersForItem = getCrafters(this.getStackItem(),validSources, new BitSet(ServerRouter.getBiggestSimpleID()), new LinkedList<IFilter>());
		
		// if you have a crafter which can make the top treeNode.getStack().getItem()
		Iterator<Pair<CraftingTemplate, List<IFilter>>> iterAllCrafters = allCraftersForItem.iterator();
		
		//a queue to store the crafters, sorted by todo; we will fill up from least-most in a balanced way.
		PriorityQueue<CraftingSorterNode> craftersSamePriority = new PriorityQueue<CraftingSorterNode>(5);
		ArrayList<CraftingSorterNode> craftersToBalance = new ArrayList<CraftingSorterNode>();
		//TODO ^ Make this a generic list
		boolean done=false;
		Pair<CraftingTemplate, List<IFilter>> lastCrafter =null;
		int currentPriority=0;
outer:
		while(!done) {
			
			/// First: Create a list of all crafters with the same priority (craftersSamePriority).	
			if(iterAllCrafters.hasNext()) {
				if(lastCrafter == null){
					lastCrafter = iterAllCrafters.next();
				}
			} else if(lastCrafter == null) {
				done=true;
			}
			
			int itemsNeeded = this.getMissingItemCount();
			
			if(lastCrafter!=null && (craftersSamePriority.isEmpty() || (currentPriority == lastCrafter.getValue1().getPriority()))) {
				currentPriority=lastCrafter.getValue1().getPriority();
				Pair<CraftingTemplate, List<IFilter>> crafter = lastCrafter;
				lastCrafter = null;
				CraftingTemplate template = crafter.getValue1();
				if(this.isCrafterUsed(template)) // then somewhere in the tree we have already used this
					continue;
				if(template.canCraft(this.getStackItem())) 
					continue; // we this is crafting something else		
				for(IFilter filter:crafter.getValue2()) { // is this filtered for some reason.
					if(filter.isBlocked() == filter.isFilteredItem(template.getResultItem().getUndamaged()) || filter.blockCrafting()) continue outer;
				}
				CraftingSorterNode cn =  new CraftingSorterNode(crafter,itemsNeeded,root,this);
//				if(cn.getWorkSetsAvailableForCrafting()>0)
					craftersSamePriority.add(cn);
				continue;
			}
			if(craftersToBalance.isEmpty() && (craftersSamePriority == null || craftersSamePriority.isEmpty())) {
				continue; //nothing at this priority was available for crafting
			}
			/// end of crafter prioriy selection.

			if(craftersSamePriority.size() == 1){ // then no need to balance.
				craftersToBalance.add(craftersSamePriority.poll());
				// automatically capped at the real amount of extra work.
				craftersToBalance.get(0).addToWorkRequest(itemsNeeded);
			} else {
//				for(CraftingSorterNode c:craftersSamePriority)
//					c.clearWorkRequest(); // so the max request isn't in there; nothing is reserved, balancing can work correctly.
				
				// go through this list, pull the crafter(s) with least work, add work until either they can not do more work,
				//   or the amount of work they have is equal to the next-least busy crafter. then pull the next crafter and repeat.
				if(!craftersSamePriority.isEmpty())
					craftersToBalance.add(craftersSamePriority.poll());
				// while we crafters that can work and we have work to do.
				while(!craftersToBalance.isEmpty() && itemsNeeded>0) {
					//while there is more, and the next crafter has the same toDo as the current one, add it to craftersToBalance.
					//  typically pulls 1 at a time, but may pull multiple, if they have the exact same todo.
					while(!craftersSamePriority.isEmpty() &&  
							craftersSamePriority.peek().currentToDo() <= craftersToBalance.get(0).currentToDo()) {
						craftersToBalance.add(craftersSamePriority.poll());
					}
					
					// find the most we can add this iteration
					int cap;
					if(!craftersSamePriority.isEmpty())
						cap = craftersSamePriority.peek().currentToDo();
					else
						cap = Integer.MAX_VALUE;
					
					//split the work between N crafters, up to "cap" (at which point we would be dividing the work between N+1 crafters.
					int floor = craftersToBalance.get(0).currentToDo();
					cap = Math.min(cap,floor + (itemsNeeded + craftersToBalance.size()-1)/craftersToBalance.size());
					
					Iterator<CraftingSorterNode> iter = craftersToBalance.iterator();
					while(iter.hasNext()){
						CraftingSorterNode crafter = iter.next();
						int request = Math.min(itemsNeeded,cap-floor);
						if(request > 0) {
							int craftingDone = crafter.addToWorkRequest(request);
							itemsNeeded -= craftingDone; // ignored under-crafting
						}
					}
	
				} // all craftersToBalance exhausted, or work completed.
				
			}// end of else more than 1 crafter at this priority
			// commit this work set.
			Iterator<CraftingSorterNode> iter = craftersToBalance.iterator();
			while(iter.hasNext()){
				CraftingSorterNode c = iter.next();
				if(c.stacksOfWorkRequested>0 && !c.addWorkPromisesToTree()) { // then it ran out of resources
					iter.remove();								
				}

			}
			itemsNeeded = this.getMissingItemCount();
			
			if(itemsNeeded <= 0)
				break outer; // we have everything we need for this crafting request

			// don't clear, because we might have under-requested, and need to consider these again
			if(!craftersToBalance.isEmpty())
				done=false;
			//craftersSamePriority.clear(); // we've extracted all we can from these priority crafters, and we still have more to do, back to the top to get the next priority level.
		}
		//LogisticsPipes.log.info("done");
		return isDone();
	}

	private class CraftingSorterNode implements Comparable<CraftingSorterNode> {
		private int stacksOfWorkRequested;
		private final int setSize;
		private final int maxWorkSetsAvailable;
		private final RequestTreeNode treeNode; // current node we are calculating

		public final Pair<CraftingTemplate, List<IFilter>> crafter;
		public final int originalToDo;

		CraftingSorterNode(Pair<CraftingTemplate, List<IFilter>> crafter, int maxCount, RequestTree tree, RequestTreeNode treeNode) {
			this.crafter = crafter;
			this.treeNode = treeNode;
			this.originalToDo = crafter.getValue1().getCrafter().getTodo();
			this.stacksOfWorkRequested = 0;
			this.setSize = crafter.getValue1().getResultStackSize();
			this.maxWorkSetsAvailable = ((treeNode.getMissingItemCount()) + setSize - 1) / setSize;
		}
		
		int calculateMaxWork(int maxSetsToCraft){
			
			int nCraftingSetsNeeded;
			if(maxSetsToCraft == 0)
				nCraftingSetsNeeded = ((treeNode.getMissingItemCount()) + setSize - 1) / setSize;
			else
				nCraftingSetsNeeded = maxSetsToCraft;
			
			if(nCraftingSetsNeeded==0) // not sure how we get here, but i've seen a stack trace later where we try to create a 0 size promise.
				return 0;
			
			CraftingTemplate template = crafter.getValue1();
			int stacks= getSubRequests(nCraftingSetsNeeded, template);
			
			
			
			return stacks;
		}


		int addToWorkRequest(int extraWork) {
			int stacksRequested = (extraWork+setSize-1)/setSize;
			stacksOfWorkRequested += stacksRequested;
			return stacksRequested*setSize;
		}

		/**
		 * Add promises for the requested work to the tree.
		 */
		boolean addWorkPromisesToTree() {
			CraftingTemplate template = crafter.getValue1();
			int setsToCraft = Math.min(this.stacksOfWorkRequested,this.maxWorkSetsAvailable);
			int setsAbleToCraft = calculateMaxWork(setsToCraft); // Deliberately outside the 0 check, because calling generatePromies(0) here clears the old ones.
			
			if(setsAbleToCraft>0) { // sanity check, as creating 0 sized promises is an exception. This should never be hit.
				//LogisticsPipes.log.info("crafting : " + setsToCraft + "sets of " + treeNode.getStack().getItem().getFriendlyName());
				//if we got here, we can at least some of the remaining amount
				List<IRelayItem> relays = new LinkedList<IRelayItem>();
				for(IFilter filter:crafter.getValue2()) {
					relays.add(filter);
				}
				LogisticsPromise job = template.generatePromise(setsAbleToCraft, relays);
				if(job.numberOfItems!=setsAbleToCraft*this.setSize)
					throw new IllegalStateException("generatePromises not creating the promisesPromised; this is goign to end badly.");
				treeNode.addPromise(job);
			} else {
				//stacksOfWorkRequested=0; // just incase we call it twice.
				//return true; // don't remove from the list if we have no w
				
				//LogisticsPipes.log.info("minor bug detected, 0 sized promise attempted. Crafting:" + treeNode.request.makeNormalStack().getUnlocalizedName());
				//LogisticsPipes.log.info("failed crafting : " + setsToCraft + "sets of " + treeNode.getStack().getItem().getFriendlyName());
			}
			boolean isDone = setsToCraft == setsAbleToCraft;
			stacksOfWorkRequested=0; // just incase we call it twice.
//			if(setsToCraft == 0) // so that we remove this node as failed when there is no work to do.
//				return false;
			return isDone;
		}

		@Override
		public int compareTo(CraftingSorterNode o) {
			return  this.currentToDo() - o.currentToDo();
		}

		public int currentToDo() {
			return this.originalToDo+this.stacksOfWorkRequested*setSize;
		}
	}

	private static List<Pair<CraftingTemplate,List<IFilter>>> getCrafters(ItemIdentifier itemToCraft, List<ExitRoute> validDestinations, BitSet layer, List<IFilter> filters) {
		List<Pair<CraftingTemplate,List<IFilter>>> crafters = new ArrayList<Pair<CraftingTemplate,List<IFilter>>>(validDestinations.size());
		List<ExitRoute> firewalls = new LinkedList<ExitRoute>();
		BitSet used = (BitSet) layer.clone();
		for(ExitRoute r : validDestinations) {
			CoreRoutedPipe pipe = r.destination.getPipe();
			if(r.containsFlag(PipeRoutingConnectionType.canRequestFrom) && !used.get(r.destination.getSimpleID())) {
				if (pipe instanceof ICraftItems){
					used.set(r.destination.getSimpleID());
					CraftingTemplate craftable = ((ICraftItems)pipe).addCrafting(itemToCraft);
					if(craftable!=null) {
						for(IFilter filter: filters) {
							if(filter.isBlocked() == filter.isFilteredItem(craftable.getResultItem().getUndamaged()) || filter.blockCrafting()) continue;
						}
						List<IFilter> list = new LinkedList<IFilter>();
						list.addAll(filters);
						crafters.add(new Pair<CraftingTemplate, List<IFilter>>(craftable, list));
					}
				}
				if(r.destination instanceof IFilteringRouter) {
					firewalls.add(r);
					used.set(r.destination.getSimpleID());
				}
			}
		}
		for(ExitRoute r:firewalls) {
			IFilter filter = ((IFilteringRouter)r.destination).getFilter();
			filters.add(filter);
			List<Pair<CraftingTemplate,List<IFilter>>> list = getCrafters(itemToCraft,((IFilteringRouter)r.destination).getRouters(), used, filters);
			filters.remove(filter);
			crafters.addAll(list);
		}
		// don't need to sort, as a sorted list is passed in and List guarantees order preservation
//		Collections.sort(crafters,new CraftingTemplate.PairPrioritizer());
		return crafters;
	}

	private int getSubRequests(int nCraftingSets, CraftingTemplate template){
		boolean failed = false;
		List<Pair<ItemIdentifierStack, IRequestItems>> stacks = template.getComponentItems(nCraftingSets);
		int workSetsAvailable = nCraftingSets;
		ArrayList<RequestTreeNode>lastNode = new ArrayList<RequestTreeNode>(stacks.size());
		for(Pair<ItemIdentifierStack,IRequestItems> stack:stacks) {
			RequestTreeNode node = new RequestTreeNode(template,stack.getValue1(), stack.getValue2(), this, RequestTree.defaultRequestFlags);
			lastNode.add(node);
			if(!node.isDone()) {
				failed = true;
			}			
		}
		List<Pair3<FluidIdentifier, Integer, IRequestFluid>> liquids = template.getComponentFluid(nCraftingSets);
		ArrayList<FluidRequestTreeNode>lastFluidNode = new ArrayList<FluidRequestTreeNode>(liquids.size());
		for(Pair3<FluidIdentifier, Integer, IRequestFluid> liquid:liquids) {
			FluidRequestTreeNode node = new FluidRequestTreeNode(liquid.getValue1(), liquid.getValue2(), liquid.getValue3(), this);
			lastFluidNode.add(node);
			if(!node.isDone()) {
				failed = true;
			}
		}
		if(failed) {
			for (RequestTreeNode n:lastNode) {
				n.destroy(); // drop the failed requests.
			}
			for (FluidRequestTreeNode n:lastFluidNode) {
				n.destroy(); // drop the failed requests.
			}
			//save last tried template for filling out the tree
			this.lastCrafterTried = template;
			//figure out how many we can actually get
			for(int i = 0; i < stacks.size(); i++) {
				workSetsAvailable = Math.min(workSetsAvailable, lastNode.get(i).getPromiseItemCount() / (stacks.get(i).getValue1().stackSize / nCraftingSets));
			}
			
			for(int i = 0; i < liquids.size(); i++) {
				workSetsAvailable = Math.min(workSetsAvailable, lastFluidNode.get(i).getPromiseFluidAmount() / (liquids.get(i).getValue2() / nCraftingSets));
			}
			
			return generateRequestTreeFor(workSetsAvailable, template);
		}
		for(ItemIdentifierStack stack:template.getByproduct()) {
			LogisticsExtraPromise extra = new LogisticsExtraPromise();
			extra.item = stack.getItem();
			extra.numberOfItems = stack.stackSize * workSetsAvailable;
			extra.sender = template.getCrafter();
			extra.provided = false;
			byproducts.add(extra);
		}
		return workSetsAvailable;
	}

	private int generateRequestTreeFor(int workSets, CraftingTemplate template) {
		
		//and try it
		ArrayList<RequestTreeNode> newChildren = new ArrayList<RequestTreeNode>();
		ArrayList<FluidRequestTreeNode> newFluidChildren = new ArrayList<FluidRequestTreeNode>();
		if(workSets>0) {
			//now set the amounts

			List<Pair<ItemIdentifierStack,IRequestItems>> stacks = template.getComponentItems(workSets);

			boolean failed = false;
			for(Pair<ItemIdentifierStack,IRequestItems> stack:stacks) {
				RequestTreeNode node = new RequestTreeNode(template,stack.getValue1(), stack.getValue2(), this, RequestTree.defaultRequestFlags);
				newChildren.add(node);
				if(!node.isDone()) {
					failed = true;
				}			
			}
			List<Pair3<FluidIdentifier, Integer, IRequestFluid>> liquids = template.getComponentFluid(workSets);
			for(Pair3<FluidIdentifier, Integer, IRequestFluid> liquid:liquids) {
				FluidRequestTreeNode node = new FluidRequestTreeNode(liquid.getValue1(), liquid.getValue2(), liquid.getValue3(), this);
				newFluidChildren.add(node);
				if(!node.isDone()) {
					failed = true;
				}
			}
			if(failed) {
				for(RequestTreeNode c:newChildren) {
					c.destroy();
				}
				for (FluidRequestTreeNode n:newFluidChildren) {
					n.destroy();
				}
				return 0;
			}
		}
		for(ItemIdentifierStack stack:template.getByproduct()) {
			LogisticsExtraPromise extra = new LogisticsExtraPromise();
			extra.item = stack.getItem();
			extra.numberOfItems = stack.stackSize * workSets;
			extra.sender = template.getCrafter();
			extra.provided = false;
			byproducts.add(extra);
		}
		return workSets;
	}

	void recurseFailedRequestTree() {
		if(this.isDone())
			return;
		if(this.lastCrafterTried == null)
			return;

		CraftingTemplate template = this.lastCrafterTried;

		int nCraftingSetsNeeded = (this.getMissingItemCount() + template.getResultStackSize() - 1) / template.getResultStackSize();

		List<Pair<ItemIdentifierStack, IRequestItems>> stacks = template.getComponentItems(nCraftingSetsNeeded);

		for(Pair<ItemIdentifierStack,IRequestItems> stack:stacks) {
			new RequestTreeNode(template, stack.getValue1(), stack.getValue2(), this, RequestTree.defaultRequestFlags);
		}

		List<Pair3<FluidIdentifier, Integer, IRequestFluid>> liquids = template.getComponentFluid(nCraftingSetsNeeded);

		for(Pair3<FluidIdentifier, Integer, IRequestFluid> liquid:liquids) {
			new FluidRequestTreeNode(liquid.getValue1(), liquid.getValue2(), liquid.getValue3(), this);
		}

		this.addPromise(template.generatePromise(nCraftingSetsNeeded, new ArrayList<IRelayItem>()));

		for(RequestTreeNode subNode : this.subRequests) {
			subNode.recurseFailedRequestTree();
		}
	}


	protected void logFailedRequestTree(RequestLog log) {
		for(RequestTreeNode node:this.subRequests) {
			node.recurseFailedRequestTree();
		}
		for(RequestTreeNode node:this.subRequests) {
			if(node instanceof RequestTree) {
				((RequestTree)node).sendMissingMessage(log);
			}
		}
	}
	
	private void destroy() {
		parentNode.remove(this);		
	}
}
