package logisticspipes.request;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IFilteringRouter;
import logisticspipes.interfaces.routing.ILiquidProvider;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.ServerRouter;
import logisticspipes.utils.IHavePriority;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.ItemMessage;
import logisticspipes.utils.LiquidIdentifier;
import logisticspipes.utils.Pair;

public class RequestManager {

	public static class workWeightedSorter implements Comparator<ExitRoute> {

		public final double distanceWeight;
		public workWeightedSorter(double distanceWeight){this.distanceWeight=distanceWeight;}
		@Override
		public int compare(ExitRoute o1, ExitRoute o2) {
			if(o1.equals(o2))
				return 0;
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
			double eps = Double.MIN_NORMAL*1024.0;
			if(c>eps)
				return flip; //round up
			if(c<-eps)
				return -flip;
			return 0;
		}
		
	}
	public static boolean request(List<ItemIdentifierStack> items, IRequestItems requester, RequestLog log) {
		LinkedList<ItemMessage> messages = new LinkedList<ItemMessage>();
		RequestTree tree = new RequestTree(new ItemIdentifierStack(ItemIdentifier.get(1,0,null), 0), requester, null);
		boolean isDone = true;
		for(ItemIdentifierStack stack:items) {
			RequestTree node = new RequestTree(stack, requester, tree);
			messages.add(new ItemMessage(stack));
			generateRequestTree(tree, node);
			isDone = isDone && node.isDone();
		}
		if(isDone) {
			handleRequestTree(tree);
			if(log != null) {
				log.handleSucessfullRequestOfList(messages);
			}
			return true;
		} else {
			if(log != null) {
				for(RequestTreeNode node:tree.subRequests) {
					recurseFailedRequestTree(tree, node);
				}
				for(RequestTreeNode node:tree.subRequests) {
					if(node instanceof RequestTree) {
						((RequestTree)node).sendMissingMessage(log);
					}
				}
			}
			return false;
		}
	}
	
	public static boolean request(ItemIdentifierStack item, IRequestItems requester, RequestLog log) {
		RequestTree tree = new RequestTree(item, requester, null);
		generateRequestTree(tree, tree);
		if(tree.isDone()) {
			handleRequestTree(tree);
			if(log != null) {
				log.handleSucessfullRequestOf(new ItemMessage(tree.getStack()));
			}
			return true;
		} else {
			if(log != null) {
				recurseFailedRequestTree(tree, tree);
				tree.sendMissingMessage(log);
			}
			return false;
		}
	}
	
	public static int requestPartial(ItemIdentifierStack item, IRequestItems requester) {
		RequestTree tree = new RequestTree(item, requester, null);
		generateRequestTree(tree, tree);
		int r = tree.getPromiseItemCount();
		if(r > 0) {
			handleRequestTree(tree);
		}
		return r;
	}

	public static void simulate(ItemIdentifierStack item, IRequestItems requester, RequestLog log) {
		RequestTree tree = new RequestTree(item, requester, null);
		generateRequestTree(tree, tree);
		if(log != null) {
			if(!tree.isDone()) {
				recurseFailedRequestTree(tree, tree);
			}
			tree.sendUsedMessage(log);
		}
	}
	
	private static List<Pair<CraftingTemplate,List<IFilter>>> getCrafters(List<ExitRoute> validDestinations, BitSet layer, List<IFilter> filters) {
		List<Pair<CraftingTemplate,List<IFilter>>> crafters = new ArrayList<Pair<CraftingTemplate,List<IFilter>>>(validDestinations.size());
		List<ExitRoute> firewalls = new LinkedList<ExitRoute>();
		BitSet used = (BitSet) layer.clone();
		for(ExitRoute r : validDestinations) {
			CoreRoutedPipe pipe = r.destination.getPipe();
			if(r.containsFlag(PipeRoutingConnectionType.canRequestFrom) && !used.get(r.destination.getSimpleID())) {
				if (pipe instanceof ICraftItems){
					used.set(r.destination.getSimpleID());
					CraftingTemplate craftable = ((ICraftItems)pipe).addCrafting();
					if(craftable!=null) {
						for(IFilter filter: filters) {
							if(filter.isBlocked() == filter.isFilteredItem(craftable.getResultStack().getItem().getUndamaged()) || filter.blockCrafting()) continue;
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
			List<Pair<CraftingTemplate,List<IFilter>>> list = getCrafters(((IFilteringRouter)r.destination).getRouters(), used, filters);
			filters.remove(filter);
			crafters.addAll(list);
		}
		// don't need to sort, as a sorted list is passed in and List guarantees order preservation
//		Collections.sort(crafters,new CraftingTemplate.PairPrioritizer());
		return crafters;
	}

	
	private static List<Pair<IProvideItems,List<IFilter>>> getProviders(List<ExitRoute> validDestinations, BitSet layer, List<IFilter> filters) {
		List<Pair<IProvideItems,List<IFilter>>> providers = new LinkedList<Pair<IProvideItems,List<IFilter>>>();
		List<ExitRoute> firewalls = new LinkedList<ExitRoute>();
		BitSet used = (BitSet) layer.clone();
		for(ExitRoute r : validDestinations) {
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
			List<Pair<IProvideItems,List<IFilter>>> list = getProviders(((IFilteringRouter)r.destination).getRouters(), used, filters);
			filters.remove(filter);
			providers.addAll(list);
		}
		return providers;
	}
	
	private static void handleRequestTree(RequestTree tree) {
		tree.fullFillAll();
	}
	private static boolean generateRequestTree(RequestTree tree, RequestTreeNode treeNode) {
		checkProvider(tree, treeNode);
		if(treeNode.isDone()) {
			return true;
		}
		checkExtras(tree, treeNode);
		if(treeNode.isDone()) {
			return true;
		}
		checkCrafting(tree, treeNode);
		return treeNode.isDone();
	}

	private static void checkExtras(RequestTree tree, RequestTreeNode treeNode) {
		LinkedList<LogisticsExtraPromise> map = tree.getExtrasFor(treeNode.getStack().getItem());
		for (LogisticsExtraPromise extraPromise : map){
			if(treeNode.isDone()) {
				break;
			}
			if(extraPromise.numberOfItems == 0)
				continue;
			boolean valid = false;
			ExitRoute source = extraPromise.sender.getRouter().getRouteTable().get(treeNode.target.getRouter().getSimpleID());
			if(source != null && !source.containsFlag(PipeRoutingConnectionType.canRouteTo)) {
				for(ExitRoute node:treeNode.target.getRouter().getIRoutersByCost()) {
					if(node.destination == extraPromise.sender.getRouter()) {
						if(node.containsFlag(PipeRoutingConnectionType.canRequestFrom)) {
							valid = true;
						}
					}
				}
			}
			if(valid) {
				extraPromise.numberOfItems = Math.min(extraPromise.numberOfItems, treeNode.getMissingItemCount());
				treeNode.addPromise(extraPromise);
			}
		}
	}

	
	private static class CraftingSorterNode implements Comparable<CraftingSorterNode>{
		private int stacksOfWorkRequested;
		private final int setSize;
		private final int maxWorkSetsAvailable;
		private final RequestTree tree; // root tree
		private final RequestTreeNode treeNode; // current node we are calculating
		private List<RequestTreeNode> lastNode; // proposed children.
		private int sizeOfLastNodeRequest; // to avoid recalc'ing when we request promises for the tree we already have .

		public final Pair<CraftingTemplate, List<IFilter>> crafter;
		public final int originalToDo;

		CraftingSorterNode(Pair<CraftingTemplate, List<IFilter>> crafter, int maxCount, RequestTree tree, RequestTreeNode treeNode) {
			this.crafter = crafter;
			this.tree = tree;
			this.treeNode = treeNode;
			this.originalToDo = crafter.getValue1().getCrafter().getTodo();
			this.stacksOfWorkRequested = 0;
			this.sizeOfLastNodeRequest = 0;
			this.setSize = crafter.getValue1().getResultStack().stackSize;
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
			List<Pair<ItemIdentifierStack,IRequestItems>> components = template.getSource();
			List<Pair<ItemIdentifierStack,IRequestItems>> stacks = new ArrayList<Pair<ItemIdentifierStack,IRequestItems>>(components.size());
			// for each thing needed to satisfy this promise
			for(Pair<ItemIdentifierStack,IRequestItems> stack : components) {
				Pair<ItemIdentifierStack, IRequestItems> pair = new Pair<ItemIdentifierStack, IRequestItems>(stack.getValue1().clone(),stack.getValue2());
				pair.getValue1().stackSize *= nCraftingSetsNeeded;
				stacks.add(pair);
			}
			
			// update how many things are currently hanging in the tree.
			sizeOfLastNodeRequest = nCraftingSetsNeeded;  
			
			boolean failed = false;
			
			int workSetsAvailable = nCraftingSetsNeeded;
			lastNode = new ArrayList<RequestTreeNode>(components.size());
			for(Pair<ItemIdentifierStack,IRequestItems> stack:stacks) {
				RequestTreeNode node = new RequestTreeNode(stack.getValue1(), stack.getValue2(), treeNode);
				lastNode.add(node);
				node.declareCrafterUsed(template);
				if(!generateRequestTree(tree, node)) {
					failed = true;
				}			
			}
			if(failed) {
				//save last tried template for filling out the tree
				treeNode.lastCrafterTried = template;
				//figure out how many we can actually get
				for(int i = 0; i < components.size(); i++) {
					workSetsAvailable = Math.min(workSetsAvailable, lastNode.get(i).getPromiseItemCount() / components.get(i).getValue1().stackSize);
				}
				return generateRequestTreeFor(workSetsAvailable);
			}
			return workSetsAvailable;
		}

		private int generateRequestTreeFor(int workSetsAvailable) {
			if(workSetsAvailable == this.sizeOfLastNodeRequest)
				return workSetsAvailable;
			sizeOfLastNodeRequest = workSetsAvailable;
			if(lastNode!=null)
				treeNode.remove(lastNode);
			
			//and try it
			lastNode = new ArrayList<RequestTreeNode>();
			if(workSetsAvailable >0) {
				//now set the amounts
				CraftingTemplate template = crafter.getValue1();
				List<Pair<ItemIdentifierStack,IRequestItems>> components = template.getSource();
				List<Pair<ItemIdentifierStack,IRequestItems>> stacks = new ArrayList<Pair<ItemIdentifierStack,IRequestItems>>(components.size());
				// for each thing needed to satisfy this promise
				for(Pair<ItemIdentifierStack,IRequestItems> stack : components) {
					Pair<ItemIdentifierStack, IRequestItems> pair = new Pair<ItemIdentifierStack, IRequestItems>(stack.getValue1().clone(),stack.getValue2());
					pair.getValue1().stackSize *= workSetsAvailable;
					stacks.add(pair);
				}
	
				boolean failed = false;
				for(Pair<ItemIdentifierStack,IRequestItems> stack:stacks) {
					RequestTreeNode node = new RequestTreeNode(stack.getValue1(), stack.getValue2(), treeNode);
					lastNode.add(node);
					node.declareCrafterUsed(template);
					if(!generateRequestTree(tree, node)) {
						failed = true;
					}			
				}
				//this should never happen...
				if(failed) {
					treeNode.remove(lastNode);
					return 0;
				}
			}
			return workSetsAvailable;
		}

		int addToWorkRequest(int extraWork) {
			int stacksRequested = (extraWork+setSize-1)/setSize;
			stacksOfWorkRequested += stacksRequested;
			return stacksRequested*setSize;
		}

		/**
		 * Add promises for the requested work to the tree.
		 */
		boolean addWorkPromisesToTree(){
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
				//LogisticsPipes.log.info("minor bug detected, 0 sized promise attempted. Crafting:" + treeNode.request.makeNormalStack().getItemName());
				//LogisticsPipes.log.info("failed crafting : " + setsToCraft + "sets of " + treeNode.getStack().getItem().getFriendlyName());
			}
			stacksOfWorkRequested=0; // just incase we call it twice.
			if(setsToCraft == 0) // so that we remove this node as failed when there is no work to do.
				return false;
			return setsToCraft == setsAbleToCraft;
		}

		@Override
		public int compareTo(CraftingSorterNode o) {
			return  this.currentToDo() - o.currentToDo();
		}

		public int currentToDo() {
			return this.originalToDo+this.stacksOfWorkRequested*setSize;
		}

		public void clearWorkRequest() {
			treeNode.remove(lastNode);
			lastNode.clear();
			stacksOfWorkRequested = 0;
			sizeOfLastNodeRequest = 0;
		}
	}

	private static void checkCrafting(RequestTree tree, RequestTreeNode treeNode) {
		
		// get all the routers
		BitSet routersIndex = ServerRouter.getRoutersInterestedIn(treeNode.getStack().getItem());
		List<ExitRoute> validSources = new ArrayList<ExitRoute>(); // get the routing table 
		for (int i = routersIndex.nextSetBit(0); i >= 0; i = routersIndex.nextSetBit(i+1)) {
			IRouter r = SimpleServiceLocator.routerManager.getRouterUnsafe(i,false);

			ExitRoute e = treeNode.target.getRouter().getDistanceTo(r);
			//ExitRoute e = r.getDistanceTo(requester.getRouter());
			if (e!=null)
				validSources.add(e);
		}
		workWeightedSorter wSorter = new workWeightedSorter(0); // distance doesn't matter, because ingredients have to be delivered to the crafter, and we can't tell how long that will take.
		Collections.sort(validSources, wSorter);
		
		List<Pair<CraftingTemplate, List<IFilter>>> allCraftersForItem = getCrafters(validSources, new BitSet(ServerRouter.getBiggestSimpleID()), new LinkedList<IFilter>());
		
		// if you have a crafter which can make the top treeNode.getStack().getItem()
		Iterator<Pair<CraftingTemplate, List<IFilter>>> iterAllCrafters = allCraftersForItem.iterator();
		
		//a queue to store the crafters, sorted by todo; we will fill up from least-most in a balanced way.
		PriorityQueue<CraftingSorterNode> craftersSamePriority = new PriorityQueue<CraftingSorterNode>(5);
		boolean done=false;
		Pair<CraftingTemplate, List<IFilter>> lastCrafter =null;
		int currentPriority=0;
		int itemsNeeded = treeNode.getMissingItemCount();
		ArrayList<CraftingSorterNode> craftersToBalance = new ArrayList<CraftingSorterNode>();
outer:
		while(!done) {
			itemsNeeded = treeNode.getMissingItemCount();
			
			/// First: Create a list of all crafters with the same priority (craftersSamePriority).	
			if(iterAllCrafters.hasNext()) {
				if(lastCrafter == null){
					lastCrafter = iterAllCrafters.next(); // a "peek" at the next thing to iterate to.
				}
			} else {
				done = true; // all crafters have been checked for crafting.
			}
			
			
			if(lastCrafter!=null && (craftersSamePriority.isEmpty() || (currentPriority == lastCrafter.getValue1().getPriority()))) {
				currentPriority=lastCrafter.getValue1().getPriority();
				Pair<CraftingTemplate, List<IFilter>> crafter = lastCrafter;
				lastCrafter = null;
				CraftingTemplate template = crafter.getValue1();
				if(treeNode.isCrafterUsed(template)) // then somewhere in the tree we have already used this
					continue;
				if(template.getResultStack().getItem() != treeNode.getStack().getItem()) 
					continue; // we this is crafting something else		
				for(IFilter filter:crafter.getValue2()) { // is this filtered for some reason.
					if(filter.isBlocked() == filter.isFilteredItem(template.getResultStack().getItem().getUndamaged()) || filter.blockCrafting()) continue outer;
				}
				CraftingSorterNode cn =  new CraftingSorterNode(crafter,itemsNeeded,tree,treeNode);
//				if(cn.getWorkSetsAvailableForCrafting()>0)
					craftersSamePriority.add(cn);
				continue;
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
				if(!c.addWorkPromisesToTree()) { // then it ran out of resources
					iter.remove();								
				}

			}
			itemsNeeded = treeNode.getMissingItemCount();
			
			if(itemsNeeded <= 0)
				break outer; // we have everything we need for this crafting request

			if(!craftersToBalance.isEmpty())
				done = false;
			// don't clear, because we might have under-requested, and need to consider these again
			//craftersSamePriority.clear(); // we've extracted all we can from these priority crafters, and we still have more to do, back to the top to get the next priority level.
		}
		//LogisticsPipes.log.info("done");
	}

	private static void recurseFailedRequestTree(RequestTree tree, RequestTreeNode treeNode) {
		if(treeNode.isDone())
			return;
		if(treeNode.lastCrafterTried == null)
			return;

		CraftingTemplate template = treeNode.lastCrafterTried;

		List<Pair<ItemIdentifierStack,IRequestItems>> components = template.getSource();
		List<Pair<ItemIdentifierStack,IRequestItems>> stacks = new ArrayList<Pair<ItemIdentifierStack,IRequestItems>>(components.size());

		int nCraftingSetsNeeded = (treeNode.getMissingItemCount() + template.getResultStack().stackSize - 1) / template.getResultStack().stackSize;

		// for each thing needed to satisfy this promise
		for(Pair<ItemIdentifierStack,IRequestItems> stack : components) {
			Pair<ItemIdentifierStack, IRequestItems> pair = new Pair<ItemIdentifierStack, IRequestItems>(stack.getValue1().clone(),stack.getValue2());
			pair.getValue1().stackSize *= nCraftingSetsNeeded;
			stacks.add(pair);
		}

		for(Pair<ItemIdentifierStack,IRequestItems> stack:stacks) {
			RequestTreeNode node = new RequestTreeNode(stack.getValue1(), stack.getValue2(), treeNode);
			node.declareCrafterUsed(template);
			generateRequestTree(tree, node);
		}

		treeNode.addPromise(template.generatePromise(nCraftingSetsNeeded, new ArrayList<IRelayItem>()));

		for(RequestTreeNode subNode : treeNode.subRequests) {
			recurseFailedRequestTree(tree, subNode);
		}
	}

	/*
	
	//if the item is the same, and the router is the same ... different stack sizes are allowed
	private class RequestPairCompare implements Comparator<Pair<ItemIdentifierStack,IRequestItems> >{

		@Override
		public int compare(Pair<ItemIdentifierStack, IRequestItems> o1,
				Pair<ItemIdentifierStack, IRequestItems> o2) {
			int c=o1.getValue1().getItem().compareTo(o2.getValue1().getItem());
			if (c==0)
				return o1.getValue2().compareTo(o2.getValue2());
			return c;
		}
		
	}
	
	*/
	
	private static void checkProvider(RequestTree tree, RequestTreeNode treeNode) {
		CoreRoutedPipe thisPipe = treeNode.target.getRouter().getPipe();
		// get all the routers
		BitSet routersIndex = ServerRouter.getRoutersInterestedIn(treeNode.getStack().getItem());
		List<ExitRoute> validSources = new ArrayList<ExitRoute>(); // get the routing table 
		for (int i = routersIndex.nextSetBit(0); i >= 0; i = routersIndex.nextSetBit(i+1)) {
			IRouter r = SimpleServiceLocator.routerManager.getRouterUnsafe(i,false);

			ExitRoute e = treeNode.target.getRouter().getDistanceTo(r);
			//ExitRoute e = r.getDistanceTo(requester.getRouter());
			if (e!=null)
				validSources.add(e);
		}
		// closer providers are good
		Collections.sort(validSources, new workWeightedSorter(1.0));
		
		for(Pair<IProvideItems, List<IFilter>> provider : getProviders(validSources, new BitSet(ServerRouter.getBiggestSimpleID()), new LinkedList<IFilter>())) {
			if(treeNode.isDone()) {
				break;
			}
			if(!thisPipe.sharesInventoryWith(provider.getValue1().getRouter().getPipe())) {
				provider.getValue1().canProvide(treeNode, tree.getAllPromissesFor(provider.getValue1(), treeNode.getStack().getItem()), provider.getValue2());
			}
		}
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
