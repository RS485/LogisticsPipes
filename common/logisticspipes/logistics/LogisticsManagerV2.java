/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logistics;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.PipeItemsRequestLogistics;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.SearchNode;
import logisticspipes.routing.ServerRouter;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.Pair;
import logisticspipes.utils.SinkReply;
import net.minecraft.item.ItemStack;

public class LogisticsManagerV2 implements ILogisticsManagerV2 {
	
	@Override
	public boolean hasDestination(ItemStack stack, boolean allowDefault, UUID sourceRouter, boolean excludeSource) {
		if (!SimpleServiceLocator.routerManager.isRouter(sourceRouter)) return false;
		Pair<UUID, SinkReply> search = getBestReply(stack, SimpleServiceLocator.routerManager.getRouter(sourceRouter), excludeSource, new ArrayList<UUID>());
		
		if (search.getValue2() == null) return false;
		
		return (allowDefault || !search.getValue2().isDefault);
	}
	
	private Pair<UUID, SinkReply> getBestReply(ItemStack item, IRouter sourceRouter, boolean excludeSource, List<UUID> jamList){
		UUID potentialDestination = null;
		SinkReply bestReply = null;
		
		for (SearchNode candidateRouter : sourceRouter.getIRoutersByCost()){
			if (excludeSource) {
				if(candidateRouter.node.getId().equals(sourceRouter.getId())) continue;
			}
			if(jamList.contains(candidateRouter.node.getId())) continue;
			
			EnumSet flags = EnumSet.copyOf(candidateRouter.connectionFlags);
			if(flags.removeAll(ServerRouter.blocksItems))
				continue;
			
			ILogisticsModule module = candidateRouter.node.getLogisticsModule();
			if (candidateRouter.node.getPipe() == null || !candidateRouter.node.getPipe().isEnabled()) continue;
			if (module == null) continue;
			SinkReply reply = module.sinksItem(item);
			if (reply == null) continue;
			if (bestReply == null){
				potentialDestination = candidateRouter.node.getId();
				bestReply = reply;
				continue;
			}
			
			if (reply.fixedPriority.ordinal() > bestReply.fixedPriority.ordinal()){
				bestReply = reply;
				potentialDestination = candidateRouter.node.getId();
				continue;
			}
			
			if (reply.fixedPriority == bestReply.fixedPriority && reply.customPriority >  bestReply.customPriority){
				bestReply = reply;
				potentialDestination = candidateRouter.node.getId();
				continue;
			}
		}
		Pair<UUID, SinkReply> result = new Pair<UUID, SinkReply>(potentialDestination, bestReply);
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
		
//		UUID potentialDestination = null;
//		SinkReply bestReply = null;
		
		Pair<UUID, SinkReply> bestReply = getBestReply(item.getItemStack(), sourceRouter, excludeSource, item.getJamList());
		
//		for (IRouter candidateRouter : sourceRouter.getIRoutersByCost()){
//			if (excludeSource && candidateRouter.getId().equals(sourceRouterUUID)) continue;
//			ILogisticsModule module = candidateRouter.getLogisticsModule();
//			if (module == null) continue;
//			SinkReply reply = module.sinksItem(ItemIdentifier.get(item.getItemStack()));
//			if (reply == null) continue;
//			if (bestReply == null){
//				potentialDestination = candidateRouter.getId();
//				bestReply = reply;
//				continue;
//			}
//			
//			if (reply.fixedPriority.ordinal() > bestReply.fixedPriority.ordinal()){
//				bestReply = reply;
//				potentialDestination = candidateRouter.getId();
//				continue;
//			}
//			
//			if (reply.fixedPriority == bestReply.fixedPriority && reply.customPriority >  bestReply.customPriority){
//				bestReply = reply;
//				potentialDestination = candidateRouter.getId();
//				continue;
//			}
//		}
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
		HashMap<ItemIdentifier, Integer> allAvailableItems = new HashMap<ItemIdentifier, Integer>();
		for(SearchNode r: validDestinations){
			if(r == null) continue;
			if (!(r.node.getPipe() instanceof IProvideItems)) continue;

			IProvideItems provider = (IProvideItems) r.node.getPipe();
			HashMap<ItemIdentifier, Integer> allItems = provider.getAllItems();
			
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

	@Override
	public LinkedList<ItemIdentifier> getCraftableItems(List<SearchNode> validDestinations) {
		LinkedList<ItemIdentifier> craftableItems = new LinkedList<ItemIdentifier>();
		for (SearchNode r : validDestinations){
			if(r == null) continue;
			if (!(r.node.getPipe() instanceof ICraftItems)) continue;
			
			ICraftItems crafter = (ICraftItems) r.node.getPipe();
			ItemIdentifier craftedItem = crafter.getCraftedItem();
			if (craftedItem != null && !craftableItems.contains(craftedItem)){
				craftableItems.add(craftedItem);
			}
		}
		return craftableItems;
	}
}
