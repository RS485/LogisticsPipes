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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IFilteringRouter;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.PipeItemsRequestLogistics;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.ServerRouter;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;

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
	public Pair3<Integer, SinkReply, List<IFilter>> hasDestination(ItemIdentifier stack, boolean allowDefault, int sourceID, List<Integer> routerIDsToExclude) {
		IRouter sourceRouter = SimpleServiceLocator.routerManager.getRouter(sourceID);
		if (sourceRouter == null) return null;
		BitSet routersIndex = ServerRouter.getRoutersInterestedIn(stack);
		List<ExitRoute> validDestinations = new ArrayList<ExitRoute>(); // get the routing table 
		for (int i = routersIndex.nextSetBit(0); i >= 0; i = routersIndex.nextSetBit(i+1)) {
			IRouter r = SimpleServiceLocator.routerManager.getRouterUnsafe(i,false);
			ExitRoute e = sourceRouter.getDistanceTo(r);
			if (e!=null && e.containsFlag(PipeRoutingConnectionType.canRouteTo))
				validDestinations.add(e);
		}
		Collections.sort(validDestinations);
		Pair3<Integer, SinkReply, List<IFilter>> search = getBestReply(stack, sourceRouter, validDestinations, true, routerIDsToExclude, new BitSet(ServerRouter.getBiggestSimpleID()), new LinkedList<IFilter>(), null,allowDefault);

		if (search.getValue2() == null) return null;

		if (!allowDefault && search.getValue2().isDefault) return null;

		return search;
	}

	/**
	 * Method used to check if a given stack has a passive sink destination at a priority.
	 * 
	 * @return Pair3 of destinationSimpleID, sinkreply, relays; null if nothing found
	 * @param stack The stack to check if it has destination.
	 * @param sourceRouter The UUID of the router pipe that wants to send the stack.
	 * @param excludeSource Boolean, true means it will not consider the pipe itself as a valid destination.
	 * @param priority The priority that the stack must have.
	 */
	@Override
	public Pair3<Integer, SinkReply, List<IFilter>> hasDestinationWithMinPriority(ItemIdentifier stack, int sourceRouter, boolean excludeSource, FixedPriority priority) {
		if (!SimpleServiceLocator.routerManager.isRouter(sourceRouter)) return null;
		Pair3<Integer, SinkReply, List<IFilter>> search = getBestReply(stack, SimpleServiceLocator.routerManager.getRouter(sourceRouter), SimpleServiceLocator.routerManager.getRouter(sourceRouter).getIRoutersByCost(), excludeSource, new ArrayList<Integer>(), new BitSet(ServerRouter.getBiggestSimpleID()), new LinkedList<IFilter>(), null,true);
		if (search.getValue2() == null) return null;
		if (search.getValue2().fixedPriority.ordinal() < priority.ordinal()) return null;
		return search;
	}


	private Pair3<Integer, SinkReply, List<IFilter>> getBestReply(ItemIdentifier stack, IRouter sourceRouter, List<ExitRoute> validDestinations, boolean excludeSource, List<Integer> jamList, BitSet layer, List<IFilter> filters, Pair3<Integer, SinkReply, List<IFilter>> result, boolean allowDefault){
		List<ExitRoute> firewall = new LinkedList<ExitRoute>();
		BitSet used = (BitSet) layer.clone();

		if(result == null) {
			result = new Pair3<Integer, SinkReply, List<IFilter>>(null, null, null);
		}
		
		for(IFilter filter:filters) {
			if(filter.isBlocked() == filter.isFilteredItem(stack.getUndamaged()) || filter.blockRouting()) return result;
		}
		
		for (ExitRoute candidateRouter : validDestinations){
			if (excludeSource) {
				if(candidateRouter.destination.getId().equals(sourceRouter.getId())) continue;
			}
			if(jamList.contains(candidateRouter.destination.getSimpleID())) continue;

			if(!candidateRouter.containsFlag(PipeRoutingConnectionType.canRouteTo)) continue;

			if(used.get(candidateRouter.destination.getSimpleID())) continue;

			used.set(candidateRouter.destination.getSimpleID());

			if(candidateRouter.destination instanceof IFilteringRouter) {
				firewall.add(candidateRouter);
			}
			
			SinkReply reply = canSink(candidateRouter.destination,sourceRouter,excludeSource,stack,result.getValue2(), false,allowDefault);
					
			if (reply == null) continue;
			if (result.getValue1() == null){
				result.setValue1(candidateRouter.destination.getSimpleID());
				result.setValue2(reply);
				List<IFilter> list = new LinkedList<IFilter>();
				list.addAll(filters);
				result.setValue3(list);
				continue;
			}

			if (reply.fixedPriority.ordinal() > result.getValue2().fixedPriority.ordinal()) {
				result.setValue1(candidateRouter.destination.getSimpleID());
				result.setValue2(reply);
				List<IFilter> list = new LinkedList<IFilter>();
				list.addAll(filters);
				result.setValue3(list);
				continue;
			}

			if (reply.fixedPriority == result.getValue2().fixedPriority && reply.customPriority >  result.getValue2().customPriority) {
				result.setValue1(candidateRouter.destination.getSimpleID());
				result.setValue2(reply);
				List<IFilter> list = new LinkedList<IFilter>();
				list.addAll(filters);
				result.setValue3(list);
				continue;
			}
		}
		for(ExitRoute n:firewall) {
			IFilter filter = ((IFilteringRouter)n.destination).getFilter();
			filters.add(filter);
			result = getBestReply(stack, sourceRouter, ((IFilteringRouter)n.destination).getRouters(), excludeSource, jamList, used, filters, result,allowDefault);
			filters.remove(filter);
		}
		if(filters.isEmpty() && result.getValue1() != null) {
			CoreRoutedPipe pipe = SimpleServiceLocator.routerManager.getRouterUnsafe(result.getValue1(),false).getPipe();
			pipe.useEnergy(result.getValue2().energyUse);
			MainProxy.sendSpawnParticlePacket(Particles.BlueParticle, pipe.getX(), pipe.getY(), pipe.getZ(), pipe.getWorld(), 10);
		}
		return result;
	}
	
		
	public static SinkReply canSink(IRouter destination, IRouter sourceRouter, boolean excludeSource,ItemIdentifier stack,SinkReply result, boolean activeRequest, boolean allowDefault) {

		SinkReply reply = null;
		LogisticsModule module = destination.getLogisticsModule();
		CoreRoutedPipe crp = destination.getPipe();
		if (module == null) return null;
		if (!(module.recievePassive() || activeRequest))
			return null;
		if (crp == null || !crp.isEnabled()) return null;
		if (excludeSource && sourceRouter !=null) {
			if(destination.getPipe().sharesInventoryWith(sourceRouter.getPipe())) return null;
		}
		if (result== null) {
			reply = module.sinksItem(stack, -1, 0, allowDefault,true);
		} else {
			reply = module.sinksItem(stack, result.fixedPriority.ordinal(), result.customPriority, allowDefault,true);
		}
		return reply;
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

		//Assert: only called server side.
		
		//If we for some reason can't get the router we can't do anything either
		IRouter sourceRouter = SimpleServiceLocator.routerManager.getRouterUnsafe(sourceRouterID,false);
		if (sourceRouter == null) return item;

		//Wipe current destination
		item.clearDestination();

		BitSet routersIndex = ServerRouter.getRoutersInterestedIn(item.getIDStack().getItem());
		List<ExitRoute> validDestinations = new ArrayList<ExitRoute>(); // get the routing table 
		for (int i = routersIndex.nextSetBit(0); i >= 0; i = routersIndex.nextSetBit(i+1)) {
			IRouter r = SimpleServiceLocator.routerManager.getRouterUnsafe(i,false);
			ExitRoute e = sourceRouter.getDistanceTo(r);
			if (e!=null && e.containsFlag(PipeRoutingConnectionType.canRouteTo))
				validDestinations.add(e);
		}
		Collections.sort(validDestinations);
		if(item.getItemStack() != null && item.getItemStack().getItem() instanceof LogisticsFluidContainer) {
			Pair<Integer, Integer> bestReply = SimpleServiceLocator.logisticsFluidManager.getBestReply(SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(item.getItemStack()), sourceRouter, item.getJamList());
			if (bestReply.getValue1() != null && bestReply.getValue1() != 0){
				item.setBufferCounter(0);
				item.setDestination(bestReply.getValue1());
			}
			return item;
		} else {
			Pair3<Integer, SinkReply, List<IFilter>> bestReply = getBestReply(item.getIDStack().getItem(), sourceRouter, validDestinations, excludeSource, item.getJamList(), new BitSet(ServerRouter.getBiggestSimpleID()), new LinkedList<IFilter>(), null,true);	
			if (bestReply.getValue1() != null && bestReply.getValue1() != 0){
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
			if (pipe.getCraftedItems() != null){
				List<ItemIdentifierStack> items = pipe.getCraftedItems();
				if(items.size()==1)
					return ("Crafter<" + items.get(0).getFriendlyName() + ">");
				return ("Crafter< MULTIPLE ITEMS >");
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
	 * @param validDestinations a list of ExitRoute of valid destinations.
	 * @return HashMap with ItemIdentifier and Integer item count of available items.
	 */
	@Override
	public HashMap<ItemIdentifier, Integer> getAvailableItems(List<ExitRoute> validDestinations) {
		//TODO: Replace this entire function wiht a fetch from the pre-built arrays (path incoming later)
		List<Map<ItemIdentifier, Integer>> items = new ArrayList<Map<ItemIdentifier, Integer>>(ServerRouter.getBiggestSimpleID());
		for(int i = 0; i < ServerRouter.getBiggestSimpleID(); i++)
			items.add(new HashMap<ItemIdentifier, Integer>());
		List<ExitRoute> filterpipes = new ArrayList<ExitRoute>();
		BitSet used = new BitSet(ServerRouter.getBiggestSimpleID());
		for(ExitRoute r: validDestinations){
			if(r == null) continue;
			if(!r.containsFlag(PipeRoutingConnectionType.canRequestFrom)) continue;
			if (!(r.destination.getPipe() instanceof IProvideItems)) {
				if(r.destination instanceof IFilteringRouter) {
					used.set(r.destination.getSimpleID(), true);
					filterpipes.add(r);
				}
				continue;
			}

			IProvideItems provider = (IProvideItems) r.destination.getPipe();
			provider.getAllItems(items.get(r.destination.getSimpleID()), new ArrayList<IFilter>(0));
			used.set(r.destination.getSimpleID(), true);
		}
		for(ExitRoute n:filterpipes) {
			List<IFilter> list = new LinkedList<IFilter>();
			list.add(((IFilteringRouter)n.destination).getFilter());
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

	private void handleAvailableSubFiltering(ExitRoute route, List<Map<ItemIdentifier, Integer>> items, List<IFilter> filters, BitSet layer) {
		List<ExitRoute> filterpipes = new ArrayList<ExitRoute>();
		BitSet used = (BitSet) layer.clone();
		for(ExitRoute n:((IFilteringRouter)route.destination).getRouters()) {
			if(n == null) continue;
			if(!n.containsFlag(PipeRoutingConnectionType.canRequestFrom)) continue;
			if(used.get(n.destination.getSimpleID())) continue;

			if (!(n.destination.getPipe() instanceof IProvideItems)) {
				if(n.destination instanceof IFilteringRouter) {
					used.set(n.destination.getSimpleID(), true);
					filterpipes.add(n);
				}
				continue;
			}
			IProvideItems provider = (IProvideItems) n.destination.getPipe();
			provider.getAllItems(items.get(route.destination.getSimpleID()), filters);
			used.set(n.destination.getSimpleID(), true);
		}
		for(ExitRoute n:filterpipes) {
			IFilter filter = ((IFilteringRouter)n.destination).getFilter();
			filters.add(filter);
			handleAvailableSubFiltering(n, items, filters, used);
			filters.remove(filter);
		}
	}

	/**
	 * @param validDestinations a List of ExitRoute of valid destinations.
	 * @return LinkedList with ItemIdentifier 
	 */
	@Override
	public LinkedList<ItemIdentifier> getCraftableItems(List<ExitRoute> validDestinations) {
		LinkedList<ItemIdentifier> craftableItems = new LinkedList<ItemIdentifier>();
		List<ExitRoute> filterpipes = new ArrayList<ExitRoute>();
		BitSet used = new BitSet(ServerRouter.getBiggestSimpleID());
		for (ExitRoute r : validDestinations){
			if(r == null) continue;
			if(!r.containsFlag(PipeRoutingConnectionType.canRequestFrom)) continue;
			if(used.get(r.destination.getSimpleID())) continue;

			if (!(r.destination.getPipe() instanceof ICraftItems)) {
				if(r.destination instanceof IFilteringRouter) {
					used.set(r.destination.getSimpleID(), true);
					filterpipes.add(r);
				}
				continue;
			}

			ICraftItems crafter = (ICraftItems) r.destination.getPipe();
			List<ItemIdentifierStack> craftedItems = crafter.getCraftedItems();
			if(craftedItems != null) {
				for(ItemIdentifierStack craftedItem:craftedItems) {
					if (craftedItem != null && !craftableItems.contains(craftedItem.getItem())){
						craftableItems.add(craftedItem.getItem());
					}
				}
			}
			used.set(r.destination.getSimpleID(), true);
		}
		for(ExitRoute n:filterpipes) {
			List<IFilter> list = new LinkedList<IFilter>();
			list.add(((IFilteringRouter)n.destination).getFilter());
			handleCraftableItemsSubFiltering(n, craftableItems, list, used);
		}
		return craftableItems;
	}

	private void handleCraftableItemsSubFiltering(ExitRoute route, LinkedList<ItemIdentifier> craftableItems, List<IFilter> filters, BitSet layer) {
		if(!route.containsFlag(PipeRoutingConnectionType.canRequestFrom)) return;
		List<ExitRoute> filterpipes = new ArrayList<ExitRoute>();
		BitSet used = (BitSet) layer.clone();
outer:
		for(ExitRoute n:((IFilteringRouter)route.destination).getRouters()) {
			if(n == null) continue;
			if(!n.containsFlag(PipeRoutingConnectionType.canRequestFrom)) continue;
			if(used.get(n.destination.getSimpleID())) continue;

			if (!(n.destination.getPipe() instanceof ICraftItems)) {
				if(n.destination instanceof IFilteringRouter) {
					used.set(n.destination.getSimpleID(), true);
					filterpipes.add(n);
				}
				continue;
			}

			ICraftItems crafter = (ICraftItems) n.destination.getPipe();
			List<ItemIdentifierStack> craftedItems = crafter.getCraftedItems();
			for(ItemIdentifierStack craftedItem:craftedItems){
				if(craftedItem != null) {
					for(IFilter filter:filters) {
						if(filter.isBlocked() == filter.isFilteredItem(craftedItem.getItem().getUndamaged()) || filter.blockCrafting()) continue outer;
					}
					if (!craftableItems.contains(craftedItem.getItem())){
						craftableItems.add(craftedItem.getItem());
					}
				}
			}
			used.set(n.destination.getSimpleID(), true);
		}
		for(ExitRoute n:filterpipes) {
			IFilter filter = ((IFilteringRouter)n.destination).getFilter();
			filters.add(filter);
			handleCraftableItemsSubFiltering(n, craftableItems, filters, used);
			filters.remove(filter);
		}
	}
}
