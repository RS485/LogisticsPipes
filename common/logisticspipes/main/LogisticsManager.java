/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.main;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.ILogisticsManager;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.logisticspipes.MessageManager;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.PipeItemsRequestLogistics;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.src.EntityPlayer;

public class LogisticsManager implements ILogisticsManager {
	
	/*public static boolean Request(LogisticsRequest originalRequest, List<IRouter> validDestinations, List<ItemMessage> errors){
		return Request(originalRequest, validDestinations, errors, null);
	}
	
	public static boolean Request(LogisticsRequest originalRequest, List<IRouter> validDestinations, List<ItemMessage> errors, EntityPlayer player){
		LogisticsTransaction transaction = new LogisticsTransaction(originalRequest);
		return Request(transaction,validDestinations,errors,player);
	}

	public static boolean Request(LogisticsTransaction transaction, List<IRouter> validDestinations, List<ItemMessage> errors, EntityPlayer player){
		return Request(transaction,validDestinations,errors,player, true);
	}
	
	public static boolean Request(LogisticsTransaction transaction, List<IRouter> validDestinations, List<ItemMessage> errors, EntityPlayer player, boolean realrequest){
		//First check all crafters
		for (IRouter r : validDestinations) {
			if (r.getPipe() instanceof ICraftItems) {
				((ICraftItems)r.getPipe()).canCraft(transaction);
			}
		}
		boolean added = true;
		while(!transaction.isDeliverable() && added){
			//Then check if we can do this without crafting any items.
			
			//Then check if we can have it delivered
			for(IRouter r : validDestinations) {
				if (r.getPipe() instanceof IProvideItems){
					((IProvideItems)r.getPipe()).canProvide(transaction);
					if (transaction.isDeliverable()) break;
				}
			}
			if (!transaction.isDeliverable()){
				added = false;
				//Check the crafters and resolve anything craftable
				
				for(LogisticsRequest remaining : transaction.getRemainingRequests()){
					
					//Check for extras
					for (LogisticsRequest extras : transaction.getRequests()){
						for (LogisticsPromise extraPromise : extras.getExtras()){
							if (remaining.isReady()) continue;
							if (extraPromise.item == remaining.getItem()) {
								//We found some spares laying around, make use of them!
								remaining.addPromise(extraPromise);
								extras.usePromise(extraPromise);
								added = true;
							}
						}
					}
					
					LinkedList<CraftingTemplate> possibleCrafts = transaction.getCrafts(remaining.getItem());
					if (possibleCrafts.isEmpty()) continue;
					outer:
					while(!remaining.isReady()){	
						for(CraftingTemplate template : possibleCrafts){
							//Loop "safeguard"
							ICraftItems crafter = template.getCrafter();
							ItemIdentifier ResultItem = template.getResultStack().getItem();
							HashMap<ItemIdentifier, Integer> totalPromised = transaction.getTotalPromised(crafter);
							if (totalPromised.containsKey(ResultItem)){
								int promisedCount = totalPromised.get(ResultItem);
								if (promisedCount > 800){ //TODO Make Settings File for this
									if(player != null)
										MessageManager.overflow(player, ResultItem);
									break outer;
								}
							}
							remaining.addPromise(template.generatePromise());
							for(LogisticsRequest newRequest : template.generateRequests()){
								transaction.addRequest(newRequest);
								added = true;
							}
						}
					}
				}
			}
		}
		
		if (!transaction.isDeliverable()){
			if (errors == null) return false;
			HashMap<ItemIdentifier, Integer> remaining = new HashMap<ItemIdentifier, Integer>();
					
			for (LogisticsRequest request : transaction.getRemainingRequests()){
				if (!remaining.containsKey(request.getItem())){
					remaining.put(request.getItem(), request.notYetAllocated());
				} else {
					remaining.put(request.getItem(), remaining.get(request.getItem()) + request.notYetAllocated());
				}
			}
			for (ItemIdentifier item : remaining.keySet()){
				errors.add(new ItemMessage(item.itemID,item.itemDamage,remaining.get(item),item.tag));
			}
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
		
//		for (LogisticsPromise promise : transaction.Promises()){
//			promise.sender.fullFill(promise, request.getDestination());
//		}
		return true;
	}
	
	public static String getBetterRouterName(IRouter r){
		
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
			if (craftedItem != null){
				craftableItems.add(craftedItem);
			}
		}
		return craftableItems;
	}
	*/
}
