/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

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


public class CraftingTemplate implements Comparable<CraftingTemplate>{
	
	protected ItemIdentifierStack _result;
	protected ICraftItems _crafter;
	protected ArrayList<Pair<ItemIdentifierStack, IRequestItems>> _required = new ArrayList<Pair<ItemIdentifierStack, IRequestItems>>(9);
	private final int priority;
	
	public CraftingTemplate(ItemIdentifierStack result, ICraftItems crafter, int priority) {
		_result = result;
		_crafter = crafter;
		this.priority = priority;
	}
	
	public void addRequirement(ItemIdentifierStack stack, IRequestItems crafter) {
		for(Pair<ItemIdentifierStack, IRequestItems> i : _required) {
			if(i.getValue1().getItem() == stack.getItem() && i.getValue2() == crafter) {
				i.getValue1().stackSize += stack.stackSize;
				return;
			}
		}
		_required.add(new Pair<ItemIdentifierStack, IRequestItems>(stack, crafter));
	}
	
	public LogisticsPromise generatePromise(int nResultSets, List<IRelayItem> relays) {
		LogisticsPromise promise = new LogisticsPromise();
		promise.item = _result.getItem();
		promise.numberOfItems = _result.stackSize * nResultSets;
		promise.sender = _crafter;
		promise.relayPoints = relays;
		return promise;
	}
	
	//TODO: refactor so that other classes don't reach through the template to the crafter.
	// needed to get the crafter todo, in order to sort
	public ICraftItems getCrafter(){
		return _crafter;
	}
	
	public int getPriority() {
		return priority;
	}

	@Override
	public int compareTo(CraftingTemplate o) {
		int c = this.priority-o.priority;
		if(c==0)
			c= _result.compareTo(o._result);
		if(c==0)
			c=_crafter.compareTo(o._crafter);
		return c;
	}

	public boolean canCraft(ItemIdentifier item) {
		return item.equals(_result);
	}

	public int getResultStackSize() {
		return _result.stackSize;
	}
	
	ItemIdentifier getResultItem() {
		return _result.getItem();
	}

	protected List<Pair<ItemIdentifierStack, IRequestItems>> getComponentItems(
			int nCraftingSetsNeeded) {
		List<Pair<ItemIdentifierStack,IRequestItems>> stacks = new ArrayList<Pair<ItemIdentifierStack,IRequestItems>>(_required.size());


		// for each thing needed to satisfy this promise
		for(Pair<ItemIdentifierStack,IRequestItems> stack : _required) {
			Pair<ItemIdentifierStack, IRequestItems> pair = new Pair<ItemIdentifierStack, IRequestItems>(stack.getValue1().clone(),stack.getValue2());
			pair.getValue1().stackSize *= nCraftingSetsNeeded;
			stacks.add(pair);
		}
		return stacks;
	}
	
	public int getSubRequests(int nCraftingSetsNeeded, RequestTreeNode currentNode){
		boolean failed = false;
		List<Pair<ItemIdentifierStack, IRequestItems>> stacks = this.getComponentItems(nCraftingSetsNeeded);
		int workSetsAvailable = nCraftingSetsNeeded;
		ArrayList<RequestTreeNode>lastNode = new ArrayList<RequestTreeNode>(stacks.size());
		for(Pair<ItemIdentifierStack,IRequestItems> stack:stacks) {
			RequestTreeNode node = new RequestTreeNode(stack.getValue1(), stack.getValue2(), currentNode);
			lastNode.add(node);
			node.declareCrafterUsed(this);
			if(!node.generateSubRequests()) {
				failed = true;
			}			
		}
		if(failed) {
			for (RequestTreeNode n:lastNode) {
				n.destroy(); // drop the failed requests.
			}
			//save last tried template for filling out the tree
			currentNode.lastCrafterTried = this;
			//figure out how many we can actually get
			for(int i = 0; i < stacks.size(); i++) {
				workSetsAvailable = Math.min(workSetsAvailable, lastNode.get(i).getPromiseItemCount() /_required.get(i).getValue1().stackSize);
			}
			return generateRequestTreeFor(workSetsAvailable, currentNode);
		}
		return workSetsAvailable;
	}
	

	protected int generateRequestTreeFor(int workSetsAvailable, RequestTreeNode currentNode) {
		
		//and try it
		ArrayList<RequestTreeNode> newChildren = new ArrayList<RequestTreeNode>();
		if(workSetsAvailable >0) {
			//now set the amounts
			
			List<Pair<ItemIdentifierStack,IRequestItems>> stacks = this.getComponentItems(workSetsAvailable);

			boolean failed = false;
			for(Pair<ItemIdentifierStack,IRequestItems> stack:stacks) {
				RequestTreeNode node = new RequestTreeNode(stack.getValue1(), stack.getValue2(), currentNode);
				newChildren.add(node);
				node.declareCrafterUsed(this);
				if(!node.generateSubRequests()) {
					failed = true;
				}			
			}
			if(failed) {
				for(RequestTreeNode c:newChildren) {
					c.destroy();
				}
				return 0;
			}
		}
		return workSetsAvailable;
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
			RequestTree node = new RequestTree(stack, requester, tree);
			messages.add(new ItemMessage(stack));
			node.generateSubRequests();
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
		tree.generateSubRequests();
		if(tree.isDone()) {
			tree.fullFillAll();
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
		tree.generateSubRequests();
		int r = tree.getPromiseItemCount();
		if(r > 0) {
			tree.fullFillAll();
		}
		return r;
	}

	public static void simulate(ItemIdentifierStack item, IRequestItems requester, RequestLog log) {
		RequestTree tree = new RequestTree(item, requester, null);
		tree.generateSubRequests();
		if(log != null) {
			if(!tree.isDone()) {
				recurseFailedRequestTree(tree, tree);
			}
			tree.sendUsedMessage(log);
		}
	}	
	
	private static void recurseFailedRequestTree(RequestTree tree, RequestTreeNode treeNode) {
		if(treeNode.isDone())
			return;
		if(treeNode.lastCrafterTried == null)
			return;

		CraftingTemplate template = treeNode.lastCrafterTried;

		int nCraftingSetsNeeded = (treeNode.getMissingItemCount() + template.getResultStackSize() - 1) / template.getResultStackSize();

		List<Pair<ItemIdentifierStack, IRequestItems>> stacks = template.getComponentItems(nCraftingSetsNeeded);

		for(Pair<ItemIdentifierStack,IRequestItems> stack:stacks) {
			RequestTreeNode node = new RequestTreeNode(stack.getValue1(), stack.getValue2(), treeNode);
			node.declareCrafterUsed(template);
			node.generateSubRequests();
		}

		treeNode.addPromise(template.generatePromise(nCraftingSetsNeeded, new ArrayList<IRelayItem>()));

		for(RequestTreeNode subNode : treeNode.subRequests) {
			recurseFailedRequestTree(tree, subNode);
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
