/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logistics;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IFilteringRouter;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.PipeItemsRequestLogistics;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.SearchNode;
import logisticspipes.routing.ServerRouter;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.SinkReply;
import net.minecraft.item.ItemStack;

public class LogisticsManagerV2 implements ILogisticsManagerV2 {
	
	@Override
	public boolean hasDestination(ItemStack stack, boolean allowDefault, UUID sourceRouter, boolean excludeSource) {
		if (!SimpleServiceLocator.routerManager.isRouter(sourceRouter)) return false;
		Pair3<UUID, SinkReply, List<IFilter>> search = getBestReply(stack, SimpleServiceLocator.routerManager.getRouter(sourceRouter), SimpleServiceLocator.routerManager.getRouter(sourceRouter).getIRoutersByCost(), excludeSource, new ArrayList<UUID>(), new BitSet(ServerRouter.getBiggestSimpleID()), new LinkedList<IFilter>(), null);
		
		if (search.getValue2() == null) return false;
		
		return (allowDefault || !search.getValue2().isDefault);
	}
	
	private Pair3<UUID, SinkReply, List<IFilter>> getBestReply(ItemStack item, IRouter sourceRouter, List<SearchNode> validDestinations, boolean excludeSource, List<UUID> jamList, BitSet layer, List<IFilter> filters, Pair3<UUID, SinkReply, List<IFilter>> result){
		for(IFilter filter:filters) {
			if(filter.isBlocked() == filter.getFilteredItems().contains(ItemIdentifier.get(item)) || filter.blockRouting()) continue;
		}
		List<SearchNode> firewall = new LinkedList<SearchNode>();
		BitSet used = (BitSet) layer.clone();
		
		if(result == null) {
			result = new Pair3<UUID, SinkReply, List<IFilter>>(null, null, null);
		}
		
		for (SearchNode candidateRouter : validDestinations){
			if (excludeSource) {
				if(candidateRouter.node.getId().equals(sourceRouter.getId())) continue;
			}
			if(jamList.contains(candidateRouter.node.getId())) continue;
			
			if(!candidateRouter.containsFlag(PipeRoutingConnectionType.canRouteTo)) continue;
			
			if(used.get(candidateRouter.node.getSimpleID())) continue;
			
			used.set(candidateRouter.node.getSimpleID());
			
			if(candidateRouter.node instanceof IFilteringRouter) {
				firewall.add(candidateRouter);
			}
			
			ILogisticsModule module = candidateRouter.node.getLogisticsModule();
			if (candidateRouter.node.getPipe() == null || !candidateRouter.node.getPipe().isEnabled()) continue;
			if (module == null) continue;
			SinkReply reply = module.sinksItem(item);
			if (reply == null) continue;
			if (result.getValue1() == null){
				result.setValue1(candidateRouter.node.getId());
				result.setValue2(reply);
				List<IFilter> list = new LinkedList<IFilter>();
				list.addAll(filters);
				result.setValue3(list);
				continue;
			}
			
			if (reply.fixedPriority.ordinal() > result.getValue2().fixedPriority.ordinal()) {
				result.setValue1(candidateRouter.node.getId());
				result.setValue2(reply);
				List<IFilter> list = new LinkedList<IFilter>();
				list.addAll(filters);
				result.setValue3(list);
				continue;
			}
			
			if (reply.fixedPriority == result.getValue2().fixedPriority && reply.customPriority >  result.getValue2().customPriority) {
				result.setValue1(candidateRouter.node.getId());
				result.setValue2(reply);
				List<IFilter> list = new LinkedList<IFilter>();
				list.addAll(filters);
				result.setValue3(list);
				continue;
			}
		}
		for(SearchNode n:firewall) {
			IFilter filter = ((IFilteringRouter)n.node).getFilter();
			filters.add(filter);
			result = getBestReply(item, sourceRouter, ((IFilteringRouter)n.node).getRouters(), excludeSource, jamList, used, filters, result);
			filters.remove(filter);
		}
		return result;
	}
	
	
	
	@Override
	public IRoutedItem assignDestinationFor(IRoutedItem item, UUID sourceRouterUUID, boolean excludeSource) {
		
		//If the source router does not exist we can't do anything with this
		if (!SimpleServiceLocator.routerManager.isRouter(sourceRouterUUID)) return item;
		//If we for some reason can't get the router we can't do anything either
		IRouter sourceRouter = SimpleServiceLocator.routerManager.getRouter(sourceRouterUUID);
		if (sourceRouter == null) return item;
		
		//Wipe current destination
		item.changeDestination(null);
		
		Pair3<UUID, SinkReply, List<IFilter>> bestReply = getBestReply(item.getItemStack(), sourceRouter, sourceRouter.getIRoutersByCost(), excludeSource, item.getJamList(), new BitSet(ServerRouter.getBiggestSimpleID()), new LinkedList<IFilter>(), null);
		
		item.setSource(sourceRouterUUID);
		if (bestReply.getValue1() != null){
			item.setDestination(bestReply.getValue1());
			if (bestReply.getValue2().isPassive){
				if (bestReply.getValue2().isDefault){
					item.setTransportMode(TransportMode.Default);
				} else {
					item.setTransportMode(TransportMode.Passive);
				}
			}
		}
		List<IRelayItem> list = new LinkedList<IRelayItem>();
		for(IFilter filter:bestReply.getValue3()) {
			list.add(filter);
		}
		item.addRelayPoints(list);
		
		return item;
	}

	@Override
	public IRoutedItem destinationUnreachable(IRoutedItem item,	UUID currentRouter) {
		// TODO Auto-generated method stub
		return assignDestinationFor(item, currentRouter, false);
	}
	
	@Override
	public String getBetterRouterName(IRouter r){
		
		if (r.getPipe() instanceof PipeItemsCraftingLogistics){
			PipeItemsCraftingLogistics pipe = (PipeItemsCraftingLogistics) r.getPipe();
			if (pipe.getCraftedItem() != null){
				return ("Crafter<" + pipe.getCraftedItem().getFriendlyName() + ">");
			}
		}
		
		if (r.getPipe() instanceof PipeItemsProviderLogistics){
			return ("Provider");
		}
		
		if (r.getPipe() instanceof PipeLogisticsChassi) {
			return "Chassis";
		}
		if (r.getPipe() instanceof PipeItemsRequestLogistics) {
			return "Request";
		}
 
		return r.getId().toString();
				
	}

	@Override
	public HashMap<ItemIdentifier, Integer> getAvailableItems(List<SearchNode> validDestinations) {
		Map<UUID, Map<ItemIdentifier, Integer>> items = new HashMap<UUID, Map<ItemIdentifier, Integer>>();
		List<SearchNode> filterpipes = new ArrayList<SearchNode>();
		BitSet used = new BitSet(ServerRouter.getBiggestSimpleID());
		for(SearchNode r: validDestinations){
			if(r == null) continue;
			if(!r.containsFlag(PipeRoutingConnectionType.canRequestFrom)) continue;
			if (!(r.node.getPipe() instanceof IProvideItems)) {
				if(r.node instanceof IFilteringRouter) {
					used.set(r.node.getSimpleID(), true);
					filterpipes.add(r);
				}
				continue;
			}

			IProvideItems provider = (IProvideItems) r.node.getPipe();
			provider.getAllItems(items, new ArrayList<IFilter>(0));
			used.set(r.node.getSimpleID(), true);
		}
		for(SearchNode n:filterpipes) {
			List<IFilter> list = new LinkedList<IFilter>();
			list.add(((IFilteringRouter)n.node).getFilter());
			handleAvailableSubFiltering(n, items, list, used);
		}
		HashMap<ItemIdentifier, Integer> allAvailableItems = new HashMap<ItemIdentifier, Integer>();
		for(Map<ItemIdentifier, Integer> allItems:items.values()) {
			for (ItemIdentifier item : allItems.keySet()){
				if (!allAvailableItems.containsKey(item)){
					allAvailableItems.put(item, allItems.get(item));
				} else {
					allAvailableItems.put(item, allAvailableItems.get(item) + allItems.get(item));
				}
			}
		}
		return allAvailableItems;
	}
	
	private void handleAvailableSubFiltering(SearchNode r, Map<UUID, Map<ItemIdentifier, Integer>> items, List<IFilter> filters, BitSet layer) {
		List<SearchNode> filterpipes = new ArrayList<SearchNode>();
		BitSet used = (BitSet) layer.clone();
		for(SearchNode n:((IFilteringRouter)r.node).getRouters()) {
			if(n == null) continue;
			if(!n.containsFlag(PipeRoutingConnectionType.canRequestFrom)) continue;
			if(used.get(n.node.getSimpleID())) continue;
			
			if (!(n.node.getPipe() instanceof IProvideItems)) {
				if(n.node instanceof IFilteringRouter) {
					used.set(n.node.getSimpleID(), true);
					filterpipes.add(n);
				}
				continue;
			}
			IProvideItems provider = (IProvideItems) n.node.getPipe();
			provider.getAllItems(items, filters);
			used.set(n.node.getSimpleID(), true);
		}
		for(SearchNode n:filterpipes) {
			IFilter filter = ((IFilteringRouter)n.node).getFilter();
			filters.add(filter);
			handleAvailableSubFiltering(n, items, filters, used);
			filters.remove(filter);
		}
	}
	
	@Override
	public LinkedList<ItemIdentifier> getCraftableItems(List<SearchNode> validDestinations) {
		LinkedList<ItemIdentifier> craftableItems = new LinkedList<ItemIdentifier>();
		List<SearchNode> filterpipes = new ArrayList<SearchNode>();
		BitSet used = new BitSet(ServerRouter.getBiggestSimpleID());
		for (SearchNode r : validDestinations){
			if(r == null) continue;
			if(!r.containsFlag(PipeRoutingConnectionType.canRequestFrom)) continue;
			if(used.get(r.node.getSimpleID())) continue;
			
			if (!(r.node.getPipe() instanceof ICraftItems)) {
				if(r.node instanceof IFilteringRouter) {
					used.set(r.node.getSimpleID(), true);
					filterpipes.add(r);
				}
				continue;
			}
			
			ICraftItems crafter = (ICraftItems) r.node.getPipe();
			ItemIdentifier craftedItem = crafter.getCraftedItem();
			if (craftedItem != null && !craftableItems.contains(craftedItem)){
				craftableItems.add(craftedItem);
			}
			used.set(r.node.getSimpleID(), true);
		}
		for(SearchNode n:filterpipes) {
			List<IFilter> list = new LinkedList<IFilter>();
			list.add(((IFilteringRouter)n.node).getFilter());
			handleCraftableItemsSubFiltering(n, craftableItems, list, used);
		}
		return craftableItems;
	}
	
	private void handleCraftableItemsSubFiltering(SearchNode r, LinkedList<ItemIdentifier> craftableItems, List<IFilter> filters, BitSet layer) {
		if(!r.containsFlag(PipeRoutingConnectionType.canRequestFrom)) return;
		List<SearchNode> filterpipes = new ArrayList<SearchNode>();
		BitSet used = (BitSet) layer.clone();
outer:
		for(SearchNode n:((IFilteringRouter)r.node).getRouters()) {
			if(n == null) continue;
			if(!n.containsFlag(PipeRoutingConnectionType.canRequestFrom)) continue;
			if(used.get(n.node.getSimpleID())) continue;
			
			if (!(n.node.getPipe() instanceof ICraftItems)) {
				if(n.node instanceof IFilteringRouter) {
					used.set(n.node.getSimpleID(), true);
					filterpipes.add(n);
				}
				continue;
			}
			
			ICraftItems crafter = (ICraftItems) n.node.getPipe();
			ItemIdentifier craftedItem = crafter.getCraftedItem();
			for(IFilter filter:filters) {
				if(filter.isBlocked() == filter.getFilteredItems().contains(craftedItem) || filter.blockCrafting()) continue outer;
			}
			if (craftedItem != null && !craftableItems.contains(craftedItem)){
				craftableItems.add(craftedItem);
			}
			used.set(n.node.getSimpleID(), true);
		}
		for(SearchNode n:filterpipes) {
			IFilter filter = ((IFilteringRouter)n.node).getFilter();
			filters.add(filter);
			handleCraftableItemsSubFiltering(n, craftableItems, filters, used);
			filters.remove(filter);
		}
	}
}
