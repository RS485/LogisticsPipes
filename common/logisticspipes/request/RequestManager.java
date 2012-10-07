package logisticspipes.request;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.ItemMessage;
import logisticspipes.utils.Pair;

public class RequestManager {


	public static boolean request(LinkedList<ItemIdentifierStack> items, IRequestItems requester, LinkedList<IRouter> validDestinations, RequestLog log) {
		LinkedList<IProvideItems> providers = getProviders(validDestinations);
		LinkedList<CraftingTemplate> crafters = getCrafters(validDestinations);
		LinkedList<ItemMessage> messages = new LinkedList<ItemMessage>();
		RequestTree tree = new RequestTree(new ItemIdentifierStack(ItemIdentifier.get(1,0,null), 0), requester);
		for(ItemIdentifierStack stack:items) {
			RequestTree node = new RequestTree(stack, requester);
			tree.subRequests.add(node);
			messages.add(new ItemMessage(stack));
			generateRequestTree(tree, node, crafters, providers);
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
	
	public static boolean request(ItemIdentifierStack item, IRequestItems requester, List<IRouter> validDestinations, RequestLog log) {
		LinkedList<IProvideItems> providers = getProviders(validDestinations);
		LinkedList<CraftingTemplate> crafters = getCrafters(validDestinations);
		RequestTree tree = new RequestTree(item, requester);
		generateRequestTree(tree, tree, crafters, providers);
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
	
	private static LinkedList<CraftingTemplate> getCrafters(List<IRouter> validDestinations) {
		LinkedList<CraftingTemplate> crafters = new LinkedList<CraftingTemplate>();
		for(IRouter r : validDestinations) {
			CoreRoutedPipe pipe = r.getPipe();
			if (pipe instanceof ICraftItems){
				((ICraftItems)pipe).addCrafting(crafters);
			}
		}
		return crafters;
	}
	
	private static LinkedList<IProvideItems> getProviders(List<IRouter> validDestinations) {
		LinkedList<IProvideItems> providers = new LinkedList<IProvideItems>();
		for(IRouter r : validDestinations) {
			CoreRoutedPipe pipe = r.getPipe();
			if (pipe instanceof IProvideItems){
				providers.add((IProvideItems)pipe);
			}
		}
		
		return providers;
	}
	
	private static void handleRequestTree(RequestTree tree) {
		tree.fullFillAll();
		tree.registerExtras();
	}
	
	private static boolean generateRequestTree(RequestTree tree, RequestTreeNode treeNode, LinkedList<CraftingTemplate> crafters, LinkedList<IProvideItems> providers) {
		checkProvider(tree,treeNode,providers);
		if(treeNode.isDone()) {
			return true;
		}
		checkExtras(tree, treeNode);
		if(treeNode.isDone()) {
			return true;
		}
		checkCrafting(tree,treeNode,crafters,providers);
		return treeNode.isDone();
	}

	private static void checkExtras(RequestTree tree, RequestTreeNode treeNode) {
		LinkedHashMap<LogisticsPromise,RequestTreeNode> map = tree.getExtrasFor(treeNode.getStack().getItem());
		for (LogisticsPromise extraPromise : map.keySet()){
			treeNode.addPromise(extraPromise);
			map.get(extraPromise).usePromise(extraPromise);
		}
	}

	private static void checkCrafting(RequestTree tree, RequestTreeNode treeNode, LinkedList<CraftingTemplate> crafters, LinkedList<IProvideItems> providers) {
		List<RequestTreeNode> lastNode = null;
		CraftingTemplate lastNodeTemplate = null;
		boolean handled = false;
		for(CraftingTemplate template:crafters) {
			if(template.getResultStack().getItem() != treeNode.getStack().getItem()) continue;
			List<Pair<ItemIdentifierStack,IRequestItems>> stacks = new ArrayList<Pair<ItemIdentifierStack,IRequestItems>>();
			RequestTreeNode treeNodeCopy = treeNode.copy();
			while(treeNodeCopy.addPromise(template.generatePromise())) {
				for(Pair<ItemIdentifierStack,IRequestItems> stack:template.getSource()) {
					boolean done = false;
					for(Pair<ItemIdentifierStack,IRequestItems> part:stacks) {
						if(part.getValue1().getItem() == stack.getValue1().getItem() && part.getValue2() == stack.getValue2()) {
							part.getValue1().stackSize += stack.getValue1().stackSize;
							done = true;
							break;
						}
					}
					if(!done) {
						Pair<ItemIdentifierStack, IRequestItems> pair = new Pair<ItemIdentifierStack, IRequestItems>(stack.getValue1().clone(),stack.getValue2());
						stacks.add(pair);
					}
				}
			}
			boolean failed = false;
			lastNode = new ArrayList<RequestTreeNode>();
			lastNodeTemplate = template;
			for(Pair<ItemIdentifierStack,IRequestItems> stack:stacks) {
				RequestTreeNode node = new RequestTreeNode(stack.getValue1(), stack.getValue2());
				lastNode.add(node);
				treeNode.subRequests.add(node);
				if(!generateRequestTree(tree,node,getCraftersWithOutCrafter(crafters,template),providers)) {
					failed = true;
				}			
			}
			if(failed) {
				for(RequestTreeNode subNode:lastNode) {
					treeNode.subRequests.remove(subNode);
				}
				continue;
			}
			handled = true;
			while(treeNode.addPromise(template.generatePromise()));
			lastNode = null;
			break;
		}
		if(!handled) {
			if(lastNode != null && lastNodeTemplate != null) {
				while(treeNode.addPromise(lastNodeTemplate.generatePromise()));
				treeNode.subRequests.addAll(lastNode);
			}
		}
	}

	private static void checkProvider(RequestTree tree, RequestTreeNode treeNode, LinkedList<IProvideItems> providers) {
		for(IProvideItems provider : providers) {
			if(provider instanceof PipeItemsProviderLogistics && treeNode.getStack().getItem().itemID == 5) {
				System.out.println();
			}
			provider.canProvide(treeNode, tree.getAllPromissesFor(provider));
		}
	}
	
	private static LinkedList<CraftingTemplate> getCraftersWithOutCrafter(LinkedList<CraftingTemplate> crafters, CraftingTemplate crafter) {
		LinkedList<CraftingTemplate> result = new LinkedList<CraftingTemplate>();
		for(CraftingTemplate template:crafters) {
			if(template != crafter) {
				result.add(template);
			}
		}
		return result;
	}
}
