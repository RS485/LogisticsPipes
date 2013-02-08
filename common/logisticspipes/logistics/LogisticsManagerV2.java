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
import java.util.Map.Entry;

import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.ICraftMultipleItems;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IFilteringRouter;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.PipeItemsRequestLogistics;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.SearchNode;
import logisticspipes.routing.ServerRouter;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.item.ItemStack;

public class LogisticsManagerV2 implements ILogisticsManagerV2 {

	/**
	 * Method used to check if a given stack has a destination.
	 * 
	 * @return Pair3 of destinationSimpleID, sinkreply, relays; null if nothing found
	 * @param stack The stack to check if it has destination.
	 * @param allowDefault Boolean, if true then a default route will be considered a valid destination.
	 * @param sourceRouter The UUID of the router pipe that wants to send the stack.
	 * @param excludeSource Boolean, true means it will not consider the pipe itself as a valid destination.
	 */
	@Override
	public Pair3<Integer, SinkReply, List<IFilter>> hasDestination(ItemStack stack, boolean allowDefault, int sourceRouter, boolean excludeSource) {
		if (!SimpleServiceLocator.routerManager.isRouter(sourceRouter)) return null;
		Pair3<Integer, SinkReply, List<IFilter>> search = getBestReply(stack, SimpleServiceLocator.routerManager.getRouter(sourceRouter), SimpleServiceLocator.routerManager.getRouter(sourceRouter).getIRoutersByCost(), excludeSource, new ArrayList<Integer>(), new BitSet(ServerRouter.getBiggestSimpleID()), new LinkedList<IFilter>(), null);

		if (search.getValue2() == null) return null;

		if (!allowDefault && search.getValue2().isDefault) return null;

		return search;
	}

	/**
	 * Method used to check if a given stack has a destination at a priority.
	 * 
	 * @return Pair3 of destinationSimpleID, sinkreply, relays; null if nothing found
	 * @param stack The stack to check if it has destination.
	 * @param sourceRouter The UUID of the router pipe that wants to send the stack.
	 * @param excludeSource Boolean, true means it will not consider the pipe itself as a valid destination.
	 * @param priority The priority that the stack must have.
	 */
	@Override
	public Pair3<Integer, SinkReply, List<IFilter>> hasDestinationWithMinPriority(ItemStack stack, int sourceRouter, boolean excludeSource, FixedPriority priority) {
		if (!SimpleServiceLocator.routerManager.isRouter(sourceRouter)) return null;
		Pair3<Integer, SinkReply, List<IFilter>> search = getBestReply(stack, SimpleServiceLocator.routerManager.getRouter(sourceRouter), SimpleServiceLocator.routerManager.getRouter(sourceRouter).getIRoutersByCost(), excludeSource, new ArrayList<Integer>(), new BitSet(ServerRouter.getBiggestSimpleID()), new LinkedList<IFilter>(), null);
		if (search.getValue2() == null) return null;
		if (search.getValue2().fixedPriority.ordinal() < priority.ordinal()) return null;
		return search;
	}


	private Pair3<Integer, SinkReply, List<IFilter>> getBestReply(ItemStack item, IRouter sourceRouter, List<SearchNode> validDestinations, boolean excludeSource, List<Integer> jamList, BitSet layer, List<IFilter> filters, Pair3<Integer, SinkReply, List<IFilter>> result){
		for(IFilter filter:filters) {
			if(filter.isBlocked() == filter.isFilteredItem(ItemIdentifier.getUndamaged(item)) || filter.blockRouting()) continue;
		}
		List<SearchNode> firewall = new LinkedList<SearchNode>();
		BitSet used = (BitSet) layer.clone();

		if(result == null) {
			result = new Pair3<Integer, SinkReply, List<IFilter>>(null, null, null);
		}

		for (SearchNode candidateRouter : validDestinations){
			if (excludeSource) {
				if(candidateRouter.node.getId().equals(sourceRouter.getId())) continue;
			}
			if(jamList.contains(candidateRouter.node.getSimpleID())) continue;

			if(!candidateRouter.containsFlag(PipeRoutingConnectionType.canRouteTo)) continue;

			if(used.get(candidateRouter.node.getSimpleID())) continue;

			used.set(candidateRouter.node.getSimpleID());

			if(candidateRouter.node instanceof IFilteringRouter) {
				firewall.add(candidateRouter);
			}

			ILogisticsModule module = candidateRouter.node.getLogisticsModule();
			if (candidateRouter.node.getPipe() == null || !candidateRouter.node.getPipe().isEnabled()) continue;
			if (excludeSource) {
				if(candidateRouter.node.getPipe().sharesInventoryWith(sourceRouter.getPipe())) continue;
			}
			if (module == null) continue;
			SinkReply reply = null;
			if (result.getValue2() == null) {
				reply = module.sinksItem(item, -1, 0);
			} else {
				reply = module.sinksItem(item, result.getValue2().fixedPriority.ordinal(), result.getValue2().customPriority);
			}
			if (reply == null) continue;
			if (result.getValue1() == null){
				result.setValue1(candidateRouter.node.getSimpleID());
				result.setValue2(reply);
				List<IFilter> list = new LinkedList<IFilter>();
				list.addAll(filters);
				result.setValue3(list);
				continue;
			}

			if (reply.fixedPriority.ordinal() > result.getValue2().fixedPriority.ordinal()) {
				result.setValue1(candidateRouter.node.getSimpleID());
				result.setValue2(reply);
				List<IFilter> list = new LinkedList<IFilter>();
				list.addAll(filters);
				result.setValue3(list);
				continue;
			}

			if (reply.fixedPriority == result.getValue2().fixedPriority && reply.customPriority >  result.getValue2().customPriority) {
				result.setValue1(candidateRouter.node.getSimpleID());
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
		if(filters.isEmpty() && result.getValue1() != null) {
			CoreRoutedPipe pipe = SimpleServiceLocator.routerManager.getRouter(result.getValue1()).getPipe();
			pipe.useEnergy(result.getValue2().energyUse);
			MainProxy.sendSpawnParticlePacket(Particles.BlueParticle, pipe.xCoord, pipe.yCoord, pipe.zCoord, pipe.worldObj, 10);
		}
		return result;
	}

	/**
	 * Will assign a destination for a IRoutedItem based on a best sink reply recieved from other pipes.
	 * @param item The item that needs to be routed.
	 * @param sourceRouterID The SimpleID of the pipe that is sending the item. (the routedItem will cache the UUID, and that the SimpleID belongs to the UUID will be checked when appropriate)
	 * @param excludeSource Boolean, true means that it wont set the source as the destination.
	 * @return IRoutedItem with a newly assigned destination
	 */
	@Override
	public IRoutedItem assignDestinationFor(IRoutedItem item, int sourceRouterID, boolean excludeSource) {

		//If the source router does not exist we can't do anything with this
		if (!SimpleServiceLocator.routerManager.isRouter(sourceRouterID)) return item;
		//If we for some reason can't get the router we can't do anything either
		IRouter sourceRouter = SimpleServiceLocator.routerManager.getRouter(sourceRouterID);
		if (sourceRouter == null) return item;

		//Wipe current destination
		item.clearDestination();

		Pair3<Integer, SinkReply, List<IFilter>> bestReply = getBestReply(item.getItemStack(), sourceRouter, sourceRouter.getIRoutersByCost(), excludeSource, item.getJamList(), new BitSet(ServerRouter.getBiggestSimpleID()), new LinkedList<IFilter>(), null);

		if (bestReply.getValue1() != null){
			item.setBufferCounter(0);
			item.setDestination(bestReply.getValue1());
			if (bestReply.getValue2().isPassive){
				if (bestReply.getValue2().isDefault){
					item.setTransportMode(TransportMode.Default);
				} else {
					item.setTransportMode(TransportMode.Passive);
				}
			} else {
				item.setTransportMode(TransportMode.Active);
			}
			List<IRelayItem> list = new LinkedList<IRelayItem>();
			if(bestReply.getValue3() != null) {
				for(IFilter filter:bestReply.getValue3()) {
					list.add(filter);
				}
			}
			item.addRelayPoints(list);
		}

		return item;
	}

	/**
	 * If there is a better router name available, it will return it.  Else, it will return the UUID as a string.
	 * @param r The IRouter that you want the name for.
	 * @return String with value of a better name if available, else just the UUID as a string.
	 */
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

	/**
	 * @param validDestinations a list of SearchNode of valid destinations.
	 * @return HashMap with ItemIdentifier and Integer item count of available items.
	 */
	@Override
	public HashMap<ItemIdentifier, Integer> getAvailableItems(List<SearchNode> validDestinations) {
		//TODO: Replace this entire function wiht a fetch from the pre-built arrays (path incoming later)
		List<Map<ItemIdentifier, Integer>> items = new ArrayList<Map<ItemIdentifier, Integer>>(ServerRouter.getBiggestSimpleID());
		for(int i = 0; i < ServerRouter.getBiggestSimpleID(); i++)
			items.add(new HashMap<ItemIdentifier, Integer>());
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
			provider.getAllItems(items.get(r.node.getSimpleID()), new ArrayList<IFilter>(0));
			used.set(r.node.getSimpleID(), true);
		}
		for(SearchNode n:filterpipes) {
			List<IFilter> list = new LinkedList<IFilter>();
			list.add(((IFilteringRouter)n.node).getFilter());
			handleAvailableSubFiltering(n, items, list, used);
		}
		
		//TODO: Fix this doubly nested list
		HashMap<ItemIdentifier, Integer> allAvailableItems = new HashMap<ItemIdentifier, Integer>();
		for(Map<ItemIdentifier, Integer> allItems:items) {
			for (Entry<ItemIdentifier, Integer> item : allItems.entrySet()){
				Integer currentItem=allAvailableItems.get(item.getKey());
				if (currentItem==null){
					allAvailableItems.put(item.getKey(), item.getValue());
				} else {
					allAvailableItems.put(item.getKey(), currentItem + item.getValue());
				}
			}
		}
		return allAvailableItems;
	}

	private void handleAvailableSubFiltering(SearchNode r, List<Map<ItemIdentifier, Integer>> items, List<IFilter> filters, BitSet layer) {
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
			provider.getAllItems(items.get(r.node.getSimpleID()), filters);
			used.set(n.node.getSimpleID(), true);
		}
		for(SearchNode n:filterpipes) {
			IFilter filter = ((IFilteringRouter)n.node).getFilter();
			filters.add(filter);
			handleAvailableSubFiltering(n, items, filters, used);
			filters.remove(filter);
		}
	}

	/**
	 * @param validDestinations a List of SearchNode of valid destinations.
	 * @return LinkedList with ItemIdentifier 
	 */
	@Override
	public LinkedList<ItemIdentifier> getCraftableItems(List<SearchNode> validDestinations) {
		LinkedList<ItemIdentifier> craftableItems = new LinkedList<ItemIdentifier>();
		List<SearchNode> filterpipes = new ArrayList<SearchNode>();
		BitSet used = new BitSet(ServerRouter.getBiggestSimpleID());
		for (SearchNode r : validDestinations)
		{
			if(r == null) continue;
			if(!r.containsFlag(PipeRoutingConnectionType.canRequestFrom)) continue;
			if(used.get(r.node.getSimpleID())) continue;

			
			if (!(r.node.getPipe() instanceof ICraftItems || r.node.getPipe() instanceof ICraftMultipleItems)) 

			{	
				if(r.node instanceof IFilteringRouter) 
				{
					used.set(r.node.getSimpleID(), true);
					filterpipes.add(r);
				}
				continue;
			}

			if(r.node.getPipe() instanceof ICraftItems)
			{
				ICraftItems crafter = (ICraftItems) r.node.getPipe();
				ItemIdentifier craftedItem = crafter.getCraftedItem();
				if (craftedItem != null && !craftableItems.contains(craftedItem))
				{
					craftableItems.add(craftedItem);
				}
			}
			else
			{
				ICraftMultipleItems crafter = (ICraftMultipleItems) r.node.getPipe();
				crafter.getCraftedItems(craftableItems);
			}
			used.set(r.node.getSimpleID(), true);
		}

		for(SearchNode n:filterpipes) 
		{
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

			if (!(n.node.getPipe() instanceof ICraftItems || n.node.getPipe() instanceof ICraftMultipleItems)) {
				if(n.node instanceof IFilteringRouter) {
					used.set(n.node.getSimpleID(), true);
					filterpipes.add(n);
				}
				continue;
			}

			if(n.node.getPipe() instanceof ICraftItems)
			{
				ICraftItems crafter = (ICraftItems) n.node.getPipe();
				ItemIdentifier craftedItem = crafter.getCraftedItem();
				for(IFilter filter:filters) {
					if(filter.isBlocked() == filter.isFilteredItem(craftedItem) || filter.blockCrafting()) continue outer;
				}
				if (craftedItem != null && !craftableItems.contains(craftedItem)){
					craftableItems.add(craftedItem);
				}
			}

			else
			{
				ICraftMultipleItems crafter = (ICraftMultipleItems) n.node.getPipe();
				List<ItemIdentifier> temp = new LinkedList<ItemIdentifier>();
				crafter.getCraftedItems(temp);
				block:
					for(ItemIdentifier item : temp)
					{
						for(IFilter filter:filters) {
							if(filter.isBlocked() == filter.isFilteredItem(item) || filter.blockCrafting()) continue block;
						}
						craftableItems.add(item);
					}
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
