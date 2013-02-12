package logisticspipes.request;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.ICraftMultipleItems;
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
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.ItemMessage;
import logisticspipes.utils.LiquidIdentifier;
import logisticspipes.utils.Pair;

public class RequestManager {

	public static boolean request(List<ItemIdentifierStack> items, IRequestItems requester, List<ExitRoute> validDestinations, RequestLog log) {
		LinkedList<ItemMessage> messages = new LinkedList<ItemMessage>();
		RequestTree tree = new RequestTree(new ItemIdentifierStack(ItemIdentifier.get(1,0,null), 0), requester,null);
		for(ItemIdentifierStack stack:items) {
			RequestTree node = new RequestTree(stack, requester, tree);
			messages.add(new ItemMessage(stack));
			generateRequestTree(tree, node, requester);
		}
		if(tree.isAllDone()) {
			handleRequestTree(tree);
			if(log != null) {
				log.handleSucessfullRequestOfList(messages);
			}
			return true;
		} else {
			if(log != null) {
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
		generateRequestTree(tree, tree, requester);
		if(tree.isAllDone()) {
			handleRequestTree(tree);
			if(log != null) {
				log.handleSucessfullRequestOf(new ItemMessage(tree.getStack()));
			}
			return true;
		} else {
			if(log != null) {
				tree.sendMissingMessage(log);
			}
			return false;
		}
	}
	
	public static void simulate(ItemIdentifierStack item, IRequestItems requester, RequestLog log) {
		RequestTree tree = new RequestTree(item, requester, null);
		generateRequestTree(tree, tree, requester);
		if(log != null) {
			tree.sendUsedMessage(log);
		}
	}
	
	private static List<Pair<CraftingTemplate,List<IFilter>>> getCrafters(List<ExitRoute> validDestinations, BitSet layer, List<IFilter> filters) {
		List<Pair<CraftingTemplate,List<IFilter>>> crafters = new ArrayList<Pair<CraftingTemplate,List<IFilter>>>(validDestinations.size());
		List<ExitRoute> firewalls = new LinkedList<ExitRoute>();
		BitSet used = (BitSet) layer.clone();
		for(ExitRoute r : validDestinations) {
			CoreRoutedPipe pipe = r.root.getPipe();
			if(r.containsFlag(PipeRoutingConnectionType.canRequestFrom) && !used.get(r.root.getSimpleID())) {
				if (pipe instanceof ICraftItems || pipe instanceof ICraftMultipleItems){
					used.set(r.root.getSimpleID());
					if(pipe instanceof ICraftMultipleItems)	{
						List<CraftingTemplate> lst = new LinkedList<CraftingTemplate>();
						((ICraftMultipleItems)pipe).addCraftings(lst);
						for(CraftingTemplate craftable : lst)
						{
							if(craftable!=null) {
								for(IFilter filter: filters) {
									if(filter.isBlocked() == filter.isFilteredItem(craftable.getResultStack().getItem()) || filter.blockCrafting()) continue;
								}
								List<IFilter> list = new LinkedList<IFilter>();
								list.addAll(filters);
								crafters.add(new Pair<CraftingTemplate, List<IFilter>>(craftable, list));
							}
						}
					} else {
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
				}
				if(r.root instanceof IFilteringRouter) {
					firewalls.add(r);
					used.set(r.root.getSimpleID());
				}
			}		
		}
		for(ExitRoute r:firewalls) {
			IFilter filter = ((IFilteringRouter)r.root).getFilter();
			filters.add(filter);
			List<Pair<CraftingTemplate,List<IFilter>>> list = getCrafters(((IFilteringRouter)r.root).getRouters(), used, filters);
			filters.remove(filter);
			crafters.addAll(list);
		}
		Collections.sort(crafters,new CraftingTemplate.PairPrioritizer());
		return crafters;
	}

	
	private static List<Pair<IProvideItems,List<IFilter>>> getProviders(List<ExitRoute> validDestinations, BitSet layer, List<IFilter> filters) {
		List<Pair<IProvideItems,List<IFilter>>> providers = new LinkedList<Pair<IProvideItems,List<IFilter>>>();
		List<ExitRoute> firewalls = new LinkedList<ExitRoute>();
		BitSet used = (BitSet) layer.clone();
		for(ExitRoute r : validDestinations) {
			if(r.containsFlag(PipeRoutingConnectionType.canRouteTo) && !used.get(r.root.getSimpleID())) {
				CoreRoutedPipe pipe = r.root.getPipe();
				if (pipe instanceof IProvideItems) {
					List<IFilter> list = new LinkedList<IFilter>();
					list.addAll(filters);
					providers.add(new Pair<IProvideItems,List<IFilter>>((IProvideItems)pipe, list));
					used.set(r.root.getSimpleID());
				}
				if(r.root instanceof IFilteringRouter) {
					firewalls.add(r);
					used.set(r.root.getSimpleID());
				}
			}
		}
		for(ExitRoute r:firewalls) {
			IFilter filter = ((IFilteringRouter)r.root).getFilter();
			filters.add(filter);
			List<Pair<IProvideItems,List<IFilter>>> list = getProviders(((IFilteringRouter)r.root).getRouters(), used, filters);
			filters.remove(filter);
			providers.addAll(list);
		}
		return providers;
	}
	
	private static void handleRequestTree(RequestTree tree) {
		tree.fullFillAll();
	}
	private static boolean generateRequestTree(RequestTree tree, RequestTreeNode treeNode, IRequestItems requester) {
		checkProvider(tree,treeNode,requester);
		if(treeNode.isDone()) {
			return true;
		}
		checkExtras(tree, treeNode);
		if(treeNode.isDone()) {
			return true;
		}
		checkCrafting(tree,treeNode,requester);
		return treeNode.isAllDone();
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

	private static void checkCrafting(RequestTree tree, RequestTreeNode treeNode, IRequestItems requester) {
		List<RequestTreeNode> lastNode = null;
		CraftingTemplate lastNodeTemplate = null;
		int nCraftingSetsNeeded = 0;
		
		// get all the routers
		Set<IRouter> routers = ServerRouter.getRoutersInterestedIn(treeNode.getStack().getItem());
		List<ExitRoute> validSources = new ArrayList(routers.size()); // get the routing table 
		for(IRouter r:routers){
			ExitRoute e = r.getDistanceTo(requester.getRouter());
			if (e!=null)
				validSources.add(e);
		}
		Collections.sort(validSources);
		
		List<Pair<CraftingTemplate, List<IFilter>>> crafters = getCrafters(validSources, new BitSet(ServerRouter.getBiggestSimpleID()), new LinkedList<IFilter>());
		
		// if you have a crafter which can make the top treeNode.getStack().getItem()
		boolean handled = false;
outer:
		for(Pair<CraftingTemplate, List<IFilter>> crafter:crafters) {
			CraftingTemplate template = crafter.getValue1();
			if(treeNode.isCrafterUsed(template)) // then somewhere in the tree we have already used this
				continue;
			
			if(template.getResultStack().getItem() != treeNode.getStack().getItem()) continue;		
			for(IFilter filter:crafter.getValue2()) {
				if(filter.isBlocked() == filter.isFilteredItem(template.getResultStack().getItem().getUndamaged()) || filter.blockCrafting()) continue outer;
			}
			List<Pair<ItemIdentifierStack,IRequestItems>> stacks = new ArrayList<Pair<ItemIdentifierStack,IRequestItems>>();

			nCraftingSetsNeeded = (treeNode.getMissingItemCount() + template.getResultStack().stackSize - 1) / template.getResultStack().stackSize;
			
			// for each thing needed to satisfy this promise
			for(Pair<ItemIdentifierStack,IRequestItems> stack:template.getSource()) {
				boolean done = false;
				//search for an existing requests from here and it to stacks <requester,item>
				for(Pair<ItemIdentifierStack,IRequestItems> part:stacks) {
					if(part.getValue1().getItem() == stack.getValue1().getItem() && part.getValue2() == stack.getValue2()) {
						part.getValue1().stackSize += stack.getValue1().stackSize * nCraftingSetsNeeded;
						done = true;
						break;
					}
				}
				if(!done) {
					//if its a new request, add it to the end.
					Pair<ItemIdentifierStack, IRequestItems> pair = new Pair<ItemIdentifierStack, IRequestItems>(stack.getValue1().clone(),stack.getValue2() );
					pair.getValue1().stackSize *= nCraftingSetsNeeded;
					stacks.add(pair);
				}
			}
			
			boolean failed = false;
			
			lastNode = new ArrayList<RequestTreeNode>();
			lastNodeTemplate = template;
			for(Pair<ItemIdentifierStack,IRequestItems> stack:stacks) {
				RequestTreeNode node = new RequestTreeNode(stack.getValue1(), stack.getValue2(), treeNode);
				lastNode.add(node);
				node.declareCrafterUsed(template);
				if(!generateRequestTree(tree,node,template.getCrafter())) {
					failed = true;
				}			
			}
			if(failed) {
				for(RequestTreeNode subNode:lastNode) {
					treeNode.remove(subNode);
				}
				continue;
			}
			handled = true;
			List<IRelayItem> relays = new LinkedList<IRelayItem>();
			for(IFilter filter:crafter.getValue2()) {
				relays.add(filter);
			}
			treeNode.addPromise(template.generatePromise(nCraftingSetsNeeded, relays));
			lastNode = null;
			break;
		}
		if(!handled) {
			if(lastNode != null && lastNodeTemplate != null) {
				treeNode.addPromise(lastNodeTemplate.generatePromise(nCraftingSetsNeeded, new ArrayList<IRelayItem>()));
				treeNode.subRequests.addAll(lastNode);
			}
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
	
	private static void checkProvider(RequestTree tree, RequestTreeNode treeNode, IRequestItems requester) {
		CoreRoutedPipe thisPipe = requester.getRouter().getPipe();
		// get all the routers
		Set<IRouter> routers = ServerRouter.getRoutersInterestedIn(treeNode.getStack().getItem());
		List<ExitRoute> validSources = new ArrayList(routers.size()); // get the routing table 
		for(IRouter r:routers){
				ExitRoute e = r.getDistanceTo(requester.getRouter());
				if (e!=null)
					validSources.add(e);
		}
		Collections.sort(validSources);
		
		for(Pair<IProvideItems, List<IFilter>> provider : getProviders(validSources, new BitSet(ServerRouter.getBiggestSimpleID()), new LinkedList<IFilter>())) {
			if(treeNode.isDone()) {
				break;
			}
			if(!thisPipe.sharesInventoryWith(provider.getValue1().getRouter().getPipe())) {
				provider.getValue1().canProvide(treeNode, tree.getAllPromissesFor(provider.getValue1()), provider.getValue2());
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
