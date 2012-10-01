/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.logisticspipes.modules.SinkReply;
import logisticspipes.main.ItemMessage;
import logisticspipes.main.LogisticsPromise;
import logisticspipes.main.LogisticsRequest;
import logisticspipes.main.LogisticsTransaction;
import logisticspipes.main.SimpleServiceLocator;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.PipeItemsRequestLogistics;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.request.CraftingTemplate;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.Pair;
import net.minecraft.src.ItemStack;

public class LogisticsManagerV2 implements ILogisticsManagerV2 {
	
	@Override
	public boolean hasDestination(ItemStack stack, boolean allowDefault, UUID sourceRouter, boolean excludeSource) {
		if (!SimpleServiceLocator.routerManager.isRouter(sourceRouter)) return false;
		Pair<UUID, SinkReply> search = getBestReply(stack, SimpleServiceLocator.routerManager.getRouter(sourceRouter), excludeSource);
		
		if (search.getValue2() == null) return false;
		
		return (allowDefault || !search.getValue2().isDefault);
	}
	
	private Pair<UUID, SinkReply> getBestReply(ItemStack item, IRouter sourceRouter, boolean excludeSource){
		UUID potentialDestination = null;
		SinkReply bestReply = null;
		
		for (IRouter candidateRouter : sourceRouter.getIRoutersByCost()){
			if (excludeSource && candidateRouter.getId().equals(sourceRouter.getId())) continue;
			ILogisticsModule module = candidateRouter.getLogisticsModule();
			if (candidateRouter.getPipe() == null || !candidateRouter.getPipe().isEnabled()) continue;
			if (module == null) continue;
			SinkReply reply = module.sinksItem(item);
			if (reply == null) continue;
			if (bestReply == null){
				potentialDestination = candidateRouter.getId();
				bestReply = reply;
				continue;
			}
			
			if (reply.fixedPriority.ordinal() > bestReply.fixedPriority.ordinal()){
				bestReply = reply;
				potentialDestination = candidateRouter.getId();
				continue;
			}
			
			if (reply.fixedPriority == bestReply.fixedPriority && reply.customPriority >  bestReply.customPriority){
				bestReply = reply;
				potentialDestination = candidateRouter.getId();
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
		item.setDestination(null);
		
//		UUID potentialDestination = null;
//		SinkReply bestReply = null;
		
		Pair<UUID, SinkReply> bestReply = getBestReply(item.getItemStack(), sourceRouter, excludeSource);
		
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

	/*
	@Override
	public boolean request(LogisticsRequest originalRequest, List<IRouter> validDestinations, List<ItemMessage> errors){
		LogisticsTransaction transaction = new LogisticsTransaction(originalRequest, true);
		return request(transaction,validDestinations,errors);
	}

	@Override
	public boolean request(LogisticsTransaction transaction, List<IRouter> validDestinations, List<ItemMessage> errors){
		return request(transaction, validDestinations, errors, true, false);
	}

	@Override
	public boolean request(LogisticsTransaction transaction, List<IRouter> validDestinations, List<ItemMessage> errors, boolean realrequest, boolean denyCrafterAdding){
		//Add all crafting templates
		transaction.setRealRequest(realrequest);

		addCraftingTemplates(transaction, validDestinations, realrequest, denyCrafterAdding);
		
		//Then check if we can have it delivered
		boolean result = checkProviders(transaction, validDestinations);

		//Then check if we can do this without crafting any items.
		if (!result) {
			
			//Check the crafters and resolve anything craftable
			for(LogisticsRequest remaining : transaction.getRemainingRequests()) {
				
				//Check for extras
				checkExtras(transaction, remaining);
				
				checkCrafting(transaction, remaining, validDestinations, errors);
				
				checkProviders(transaction, validDestinations);
			}
		}
		
		for (IRouter r : validDestinations) {
			if (r.getPipe() instanceof ICraftItems) {
				//((ICraftItems)r.getPipe()).canCraft(transaction);
			}
		}
		
		if (!transaction.isDeliverable()) {
			if (errors == null) return false;
			HashMap<ItemIdentifier, Integer> remaining = new HashMap<ItemIdentifier, Integer>();
					
			for (LogisticsRequest request : transaction.getRemainingRequests()){
				LinkedList<CraftingTemplate> possibleCrafts = transaction.getCrafts(request.getItem());
				if (!possibleCrafts.isEmpty()) continue;
				if (!remaining.containsKey(request.getItem())){
					remaining.put(request.getItem(), request.notYetAllocated());
				} else {
					remaining.put(request.getItem(), remaining.get(request.getItem()) + request.notYetAllocated());
				}
			}
			for (ItemIdentifier item : remaining.keySet()){
				errors.add(new ItemMessage(item.itemID,item.itemDamage,remaining.get(item),item.tag));
			}
			ItemMessage.compress(errors);
			return false;
		}

		if(!realrequest) {
			return true;
		}
		
		if (transaction.getRequests().getFirst() != null){
			if(LogisticsPipes.DisplayRequests)System.out.println("*** START REQUEST FOR " + transaction.getRequests().getFirst().numberLeft() + " " + transaction.getRequests().getFirst().getItem().getFriendlyName() + " ***");			
		}
		for (LogisticsRequest request : transaction.getRequests()){
			if(LogisticsPipes.DisplayRequests)System.out.println("\tRequest for " + request.numberLeft() + " " + request.getItem().getFriendlyName());
			for(LogisticsPromise promise : request.getPromises()) {
				promise.sender.fullFill(promise, request.getDestination());
				if(LogisticsPipes.DisplayRequests)System.out.println("\t\t" + getBetterRouterName(promise.sender.getRouter()) +  "\tSENDING " +promise.numberOfItems + " " + promise.item.getFriendlyName() + " to " + getBetterRouterName(request.getDestination().getRouter()));
				if (promise.extra){
					if(LogisticsPipes.DisplayRequests)System.out.println("\t\t\t--Used extras from previous request");
				}
			}

			for (LogisticsPromise promise : request.getExtras()){
				if(LogisticsPipes.DisplayRequests)System.out.println("\t\t\t--EXTRAS: " + promise.numberOfItems + " " + promise.item.getFriendlyName());
				//Register extras that can be used in later requests
				((ICraftItems)promise.sender).registerExtras(promise.numberOfItems);
			}
		}
		if(LogisticsPipes.DisplayRequests)System.out.println("*** END REQUEST ***");

		return true;
	}
	
	private void addCraftingTemplates(LogisticsTransaction transaction, List<IRouter> validDestinations, boolean realrequest, boolean denyCrafterAdding) {
		if(!transaction.hasCraftingTemplates() && !denyCrafterAdding) {
			for (IRouter r : validDestinations) {
				if (r.getPipe() instanceof ICraftItems) {
					//((ICraftItems)r.getPipe()).canCraft(transaction);
				}
			}
		}
	}
	
	private boolean checkProviders(LogisticsTransaction transaction, List<IRouter> validDestinations) {
		for(IRouter r : validDestinations) {
			if (r.getPipe() instanceof IProvideItems){
				//((IProvideItems)r.getPipe()).canProvide(transaction);
				if (transaction.isDeliverable()) break;
			}
		}
		return transaction.isDeliverable();
	}
	
	private boolean checkExtras(LogisticsTransaction transaction, LogisticsRequest remaining) {
		boolean result = false;
		for (LogisticsRequest extras : transaction.getRequests()){
			for (LogisticsPromise extraPromise : extras.getExtras()){
				if (remaining.isReady()) continue;
				if (extraPromise.item == remaining.getItem()) {
					//We found some spares laying around, make use of them!
					remaining.addPromise(extraPromise);
					extras.usePromise(extraPromise);
					result = true;
				}
			}
		}
		return result;
	}
	
	private boolean checkCrafting(LogisticsTransaction transaction, LogisticsRequest remaining, List<IRouter> validDestinations, List<ItemMessage> errors) {
		List<ItemMessage> localErrors = new ArrayList<ItemMessage>();
		LinkedList<CraftingTemplate> possibleCrafts = transaction.getCrafts(remaining.getItem());
		if (possibleCrafts.isEmpty()) return false;
		LogisticsTransaction lastUsedTransaction = null;
		boolean changed = true;
		for(CraftingTemplate template : possibleCrafts) {
			ICraftItems crafter = template.getCrafter();

			LogisticsTransaction newtransaction = transaction.copyWithoutCrafter(crafter);
			
			lastUsedTransaction = newtransaction;
			
			LogisticsRequest localRemain = remaining.copy();
			localRemain.realRequest = false;
					
			newtransaction.addRequest(localRemain);
			
			while(!localRemain.isReady()) {
				localRemain.addPromise(template.generatePromise());
				for(LogisticsRequest newRequest : template.generateRequests()){
					newtransaction.addRequest(newRequest);
				}
			}
			
			newtransaction.removeRequest(localRemain);
			
			if(request(newtransaction, validDestinations, localErrors, false, true)) {
				localErrors.clear();
				transaction.insertRequests(newtransaction);
				while(!remaining.isReady()) {
					remaining.addPromise(template.generatePromise());
				}
				changed = true;
				break;
			}
		}
		if(remaining.isReady()) {
			return true;
		}
		if(lastUsedTransaction != null) {
			transaction.insertRequests(lastUsedTransaction);
		}
		return false;
	}
	
	private List<IRouter> copyWithoutCrafter(List<IRouter> validDestinations, ICraftItems crafter) {
		List<IRouter> newvalidDestinations = new ArrayList<IRouter>();
		for(IRouter router:validDestinations) {
			if(!router.equals(crafter.getRouter())) {
				newvalidDestinations.add(router);
			}
		}
		return newvalidDestinations;
	}
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
			PipeItemsProviderLogistics pipe = (PipeItemsProviderLogistics) r.getPipe();
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
	public HashMap<ItemIdentifier, Integer> getAvailableItems(Set<IRouter> validDestinations) {
		HashMap<ItemIdentifier, Integer> allAvailableItems = new HashMap<ItemIdentifier, Integer>();
		for(IRouter r: validDestinations){
			if(r == null) continue;
			if (!(r.getPipe() instanceof IProvideItems)) continue;

			IProvideItems provider = (IProvideItems) r.getPipe();
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
	public LinkedList<ItemIdentifier> getCraftableItems(Set<IRouter> validDestinations) {
		LinkedList<ItemIdentifier> craftableItems = new LinkedList<ItemIdentifier>();
		for (IRouter r : validDestinations){
			if(r == null) continue;
			if (!(r.getPipe() instanceof ICraftItems)) continue;
			
			ICraftItems crafter = (ICraftItems) r.getPipe();
			ItemIdentifier craftedItem = crafter.getCraftedItem();
			if (craftedItem != null && !craftableItems.contains(craftedItem)){
				craftableItems.add(craftedItem);
			}
		}
		return craftableItems;
	}
}
