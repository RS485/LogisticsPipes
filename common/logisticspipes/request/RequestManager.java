package logisticspipes.request;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IFilteringRouter;
import logisticspipes.interfaces.routing.ILiquidProvider;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.LogisticsExtraPromise;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.SearchNode;
import logisticspipes.routing.ServerRouter;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.ItemMessage;
import logisticspipes.utils.LiquidIdentifier;
import logisticspipes.utils.Pair;

public class RequestManager {

	public static boolean request(List<ItemIdentifierStack> items, IRequestItems requester, List<SearchNode> validDestinations, RequestLog log) {
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
		generateRequestTree(tree, tree, requester,true);
		if(log != null) {
				tree.sendMissingMessage(log);
		}
	}
	
	private static List<Pair<CraftingTemplate,List<IFilter>>> getCrafters(List<SearchNode> validDestinations, BitSet layer, List<IFilter> filters) {
		List<Pair<CraftingTemplate,List<IFilter>>> crafters = new ArrayList<Pair<CraftingTemplate,List<IFilter>>>(validDestinations.size());
		List<SearchNode> firewalls = new LinkedList<SearchNode>();
		BitSet used = (BitSet) layer.clone();
		for(SearchNode r : validDestinations) {
			CoreRoutedPipe pipe = r.node.getPipe();
			if(r.containsFlag(PipeRoutingConnectionType.canRequestFrom) && !used.get(r.node.getSimpleID())) {
				if (pipe instanceof ICraftItems){
					used.set(r.node.getSimpleID());
					CraftingTemplate craftable = ((ICraftItems)pipe).addCrafting();
					if(craftable!=null) {
						for(IFilter filter: filters) {
							if(filter.isBlocked() == filter.getFilteredItems().contains(craftable.getResultStack().getItem()) || filter.blockCrafting()) continue;
						}
						List<IFilter> list = new LinkedList<IFilter>();
						list.addAll(filters);
						crafters.add(new Pair<CraftingTemplate, List<IFilter>>(craftable, list));
					}
				}
				if(r.node instanceof IFilteringRouter) {
					firewalls.add(r);
					used.set(r.node.getSimpleID());
				}
			}		
		}
		for(SearchNode r:firewalls) {
			IFilter filter = ((IFilteringRouter)r.node).getFilter();
			filters.add(filter);
			List<Pair<CraftingTemplate,List<IFilter>>> list = getCrafters(((IFilteringRouter)r.node).getRouters(), used, filters);
			filters.remove(filter);
			crafters.addAll(list);
		}
		Collections.sort(crafters,new CraftingTemplate.PairPrioritizer());
		return crafters;
	}

	
	private static List<Pair<IProvideItems,List<IFilter>>> getProviders(List<SearchNode> validDestinations, BitSet layer, List<IFilter> filters) {
		List<Pair<IProvideItems,List<IFilter>>> providers = new LinkedList<Pair<IProvideItems,List<IFilter>>>();
		List<SearchNode> firewalls = new LinkedList<SearchNode>();
		BitSet used = (BitSet) layer.clone();
		for(SearchNode r : validDestinations) {
			if(r.containsFlag(PipeRoutingConnectionType.canRequestFrom) && !used.get(r.node.getSimpleID())) {
				CoreRoutedPipe pipe = r.node.getPipe();
				if (pipe instanceof IProvideItems) {
					List<IFilter> list = new LinkedList<IFilter>();
					list.addAll(filters);
					providers.add(new Pair<IProvideItems,List<IFilter>>((IProvideItems)pipe, list));
					used.set(r.node.getSimpleID());
				}
				if(r.node instanceof IFilteringRouter) {
					firewalls.add(r);
					used.set(r.node.getSimpleID());
				}
			}
		}
		for(SearchNode r:firewalls) {
			IFilter filter = ((IFilteringRouter)r.node).getFilter();
			filters.add(filter);
			List<Pair<IProvideItems,List<IFilter>>> list = getProviders(((IFilteringRouter)r.node).getRouters(), used, filters);
			filters.remove(filter);
			providers.addAll(list);
		}
		return providers;
	}
	
	private static void handleRequestTree(RequestTree tree) {
		tree.fullFillAll();
		tree.registerExtras();
	}
	private static boolean generateRequestTree(RequestTree tree, RequestTreeNode treeNode, IRequestItems requester) {
		return generateRequestTree(tree, treeNode, requester, false);
		
	}
	
	private static boolean generateRequestTree(RequestTree tree, RequestTreeNode treeNode, IRequestItems requester, boolean ignoreProviders) {

		if(!ignoreProviders)
			checkProvider(tree,treeNode,requester);
		
		if(treeNode.isDone()) {
			return true;
		}
		checkExtras(tree, treeNode);
		if(treeNode.isDone()) {
			return true;
		}
		checkCrafting(tree,treeNode,requester,ignoreProviders);
		return treeNode.isDone();
	}

	private static void checkExtras(RequestTree tree, RequestTreeNode treeNode) {
		//TODO can this be send from crafter to crafter? (Firewall, pipe flags...)
		LinkedHashMap<LogisticsExtraPromise,RequestTreeNode> map = tree.getExtrasFor(treeNode.getStack().getItem());
		for (LogisticsExtraPromise extraPromise : map.keySet()){
			if(treeNode.isDone()) {
				break;
			}
			treeNode.addPromise(extraPromise);
			map.get(extraPromise).usePromise(extraPromise);
		}
	}

	private static void checkCrafting(RequestTree tree, RequestTreeNode treeNode, IRequestItems requester, boolean ignoreProviders) {
		List<RequestTreeNode> lastNode = null;
		CraftingTemplate lastNodeTemplate = null;
		List<SearchNode> validDestinations = requester.getRouter().getIRoutersByCost();
		List<Pair<CraftingTemplate, List<IFilter>>> crafters = getCrafters(validDestinations, new BitSet(ServerRouter.getBiggestSimpleID()), new LinkedList<IFilter>());
		
		// if you have a crafter which can make the top treeNode.getStack().getItem()
		boolean handled = false;
		for(Pair<CraftingTemplate, List<IFilter>> crafter:crafters) {
			CraftingTemplate template = crafter.getValue1();
			if(treeNode.isCrafterUsed(template)) // then somewhere in the tree we have already used this
				continue;
			
			if(template.getResultStack().getItem() != treeNode.getStack().getItem()) continue;			
			List<Pair<ItemIdentifierStack,IRequestItems>> stacks = new ArrayList<Pair<ItemIdentifierStack,IRequestItems>>();

			int nCraftingSetsNeeded = (treeNode.getMissingItemCount() + template.getResultStack().stackSize - 1) / template.getResultStack().stackSize;
			
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
				if(!generateRequestTree(tree,node,template.getCrafter(),ignoreProviders)) {
					failed = true;
				}			
			}
			if(failed) {
				for(RequestTreeNode subNode:lastNode) {
					subNode.revertExtraUsage();
					treeNode.remove(subNode);
				}
				continue;
			}
			handled = true;
			List<IRelayItem> relays = new LinkedList<IRelayItem>();
			for(IFilter filter:crafter.getValue2()) {
				relays.add(filter);
			}
			while(treeNode.addPromise(template.generatePromise(relays)));
			lastNode = null;
			break;
		}
		if(!handled) {
			if(lastNode != null && lastNodeTemplate != null) {
				while(treeNode.addPromise(lastNodeTemplate.generatePromise(new ArrayList<IRelayItem>())));
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
		for(Pair<IProvideItems, List<IFilter>> provider : getProviders(requester.getRouter().getIRoutersByCost(), new BitSet(ServerRouter.getBiggestSimpleID()), new LinkedList<IFilter>())) {
			provider.getValue1().canProvide(treeNode, tree.getAllPromissesFor(provider.getValue1()), provider.getValue2());
		}
	}

	public static boolean requestLiquid(LiquidIdentifier liquid, int amount, IRequestLiquid pipe, List<SearchNode> list, RequestLog log) {
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

	private static List<ILiquidProvider> getLiquidProviders(List<SearchNode> list) {
		List<ILiquidProvider> providers = new LinkedList<ILiquidProvider>();
		for(SearchNode r : list) {
			CoreRoutedPipe pipe = r.node.getPipe();
			if (pipe instanceof ILiquidProvider){
				providers.add((ILiquidProvider)pipe);
			}
		}
		return providers;
	}
}
