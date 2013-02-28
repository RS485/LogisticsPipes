package logisticspipes.request;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IFilteringRouter;
import logisticspipes.interfaces.routing.ILiquidProvider;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LogisticsExtraPromise;
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
			double c=0;
			if(o1.destination instanceof IHavePriority){
				if(!(o2.destination instanceof IHavePriority))
					return -1;
				else
					c=((IHavePriority)o1.destination).getPriority()-((IHavePriority)o2.destination).getPriority();
				
			} else
				if(!(o2.destination instanceof IHavePriority))
					return 1;
			if(c!=0)
				return (int)c;
			c = o1.destination.getPipe().getLoadFactor()- o2.destination.getPipe().getLoadFactor();
			if(distanceWeight!=0)
				c+= (o1.distanceToDestination - o2.distanceToDestination)*distanceWeight;
			if (c==0)
				return o1.destination.getSimpleID() - o2.destination.getSimpleID();
			return (int)(c+0.5); // round up
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

	private static void checkCrafting(RequestTree tree, RequestTreeNode treeNode) {
		
		// get all the routers
		Set<IRouter> routers = ServerRouter.getRoutersInterestedIn(treeNode.getStack().getItem());
		List<ExitRoute> validSources = new ArrayList<ExitRoute>(routers.size()); // get the routing table 
		for(IRouter r:routers){
			ExitRoute e = treeNode.target.getRouter().getDistanceTo(r);
			//ExitRoute e = r.getDistanceTo(requester.getRouter());
			if (e!=null)
				validSources.add(e);
		}
		workWeightedSorter wSorter = new workWeightedSorter(0); // distance doesn't matter, because ingredients have to be delivered to the crafter, and we can't tell how long that will take.
		Collections.sort(validSources, wSorter);
		
		List<Pair<CraftingTemplate, List<IFilter>>> crafters = getCrafters(validSources, new BitSet(ServerRouter.getBiggestSimpleID()), new LinkedList<IFilter>());
		
		// if you have a crafter which can make the top treeNode.getStack().getItem()
		Iterator<Pair<CraftingTemplate, List<IFilter>>> iter = crafters.iterator();
		List<Pair<CraftingTemplate, List<IFilter>>> craftersSamePriority= new LinkedList<Pair<CraftingTemplate, List<IFilter>>>();
		boolean done=false;
		Pair<CraftingTemplate, List<IFilter>> lastCrafter =null;
outer:
		while(!done) {
			// Create a list of all crafters with the same priority.	
			if(iter.hasNext()) {
				if(lastCrafter == null)
					lastCrafter = iter.next();
			}else {
				done=true;				
			}
			
			if(lastCrafter!=null && (craftersSamePriority.isEmpty() || (craftersSamePriority.get(0).getValue1().getPriority() == lastCrafter.getValue1().getPriority()))) {
				Pair<CraftingTemplate, List<IFilter>> crafter = lastCrafter;
				lastCrafter = null;
				CraftingTemplate template = crafter.getValue1();
				if(treeNode.isCrafterUsed(template)) // then somewhere in the tree we have already used this
					continue;
				if(template.getResultStack().getItem() != treeNode.getStack().getItem()) 
					continue; // we are crafting something else		
				for(IFilter filter:crafter.getValue2()) { // is this filtered for some reason.
					if(filter.isBlocked() == filter.isFilteredItem(template.getResultStack().getItem().getUndamaged()) || filter.blockCrafting()) continue outer;
				}
				craftersSamePriority.add(crafter);
				continue;
			}
			
			
			int samePriorityCount = craftersSamePriority.size();
			int itemsNeeded = treeNode.getMissingItemCount();

			// having gathered all the items of the same
			// end selection of crafters, now to try to split the request evenly over the N crafters we have.
			for(boolean getAll:new boolean[]{false,true}) {
				// first try an even 1/N, rounded up split request for each crafter
				// then just try to get everything left from each of them.
				for(Pair<CraftingTemplate, List<IFilter>> crafter : craftersSamePriority) {
					// do the normal crafting request over those items.
					CraftingTemplate template = crafter.getValue1();
					List<Pair<ItemIdentifierStack,IRequestItems>> components = template.getSource();
					List<Pair<ItemIdentifierStack,IRequestItems>> stacks = new ArrayList<Pair<ItemIdentifierStack,IRequestItems>>(components.size());
					int nCraftingSetsNeeded;
					if(getAll)
						nCraftingSetsNeeded = ((treeNode.getMissingItemCount()) + template.getResultStack().stackSize - 1) / template.getResultStack().stackSize;
					else{
						itemsNeeded = Math.min(itemsNeeded, treeNode.getMissingItemCount());
						nCraftingSetsNeeded = (Math.min(itemsNeeded/samePriorityCount, treeNode.getMissingItemCount()) + template.getResultStack().stackSize - 1) / template.getResultStack().stackSize;
					}
					
					// for each thing needed to satisfy this promise
					for(Pair<ItemIdentifierStack,IRequestItems> stack : components) {
						Pair<ItemIdentifierStack, IRequestItems> pair = new Pair<ItemIdentifierStack, IRequestItems>(stack.getValue1().clone(),stack.getValue2());
						pair.getValue1().stackSize *= nCraftingSetsNeeded;
						stacks.add(pair);
					}
					
					boolean failed = false;
					
					int nCraftingSetsAvailable = nCraftingSetsNeeded;
					List<RequestTreeNode> lastNode = new ArrayList<RequestTreeNode>(components.size());
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
							nCraftingSetsAvailable = Math.min(nCraftingSetsAvailable, lastNode.get(i).getPromiseItemCount() / components.get(i).getValue1().stackSize);
						}
						treeNode.remove(lastNode);
						if(nCraftingSetsAvailable == 0) {
							continue; // try the next crafter at the same priority
						}
						//now set the amounts
						for(int i = 0; i < components.size(); i++) {
							stacks.get(i).getValue1().stackSize = components.get(i).getValue1().stackSize * nCraftingSetsAvailable;
						}
						//and try it
						lastNode.clear();
						failed = false;
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
							continue;
						}
					}
					//if we got here, we can at least some of the remaining amount
					List<IRelayItem> relays = new LinkedList<IRelayItem>();
					for(IFilter filter:crafter.getValue2()) {
						relays.add(filter);
					}
					treeNode.addPromise(template.generatePromise(nCraftingSetsAvailable, relays));
					itemsNeeded -= nCraftingSetsAvailable *template.getResultStack().stackSize;
					if(itemsNeeded <= 0)
						break outer; // we have everything we need for this crafting request
				}
			}
			craftersSamePriority.clear(); // we've extracted all we can from these priority crafters, and we still have more to do, back to the top to get the next priority level.
		}
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
		Set<IRouter> routers = ServerRouter.getRoutersInterestedIn(treeNode.getStack().getItem());
		List<ExitRoute> validSources = new ArrayList<ExitRoute>(routers.size()); // get the routing table 
		for(IRouter r:routers){
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
