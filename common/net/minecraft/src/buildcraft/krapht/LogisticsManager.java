/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht;

import java.io.PipedReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.mod_LogisticsPipes;
import buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsCraftingLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsProviderLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsRequestLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeLogisticsChassi;
import net.minecraft.src.buildcraft.krapht.routing.IRouter;
import net.minecraft.src.buildcraft.krapht.routing.Router;
import net.minecraft.src.buildcraft.krapht.routing.RouterManager;
import net.minecraft.src.buildcraft.logisticspipes.MessageManager;
import net.minecraft.src.krapht.ItemIdentifier;

public class LogisticsManager implements ILogisticsManager {
	private class LogisticsValue {
		int TotalThisCycle = 0;
		int CompletedThisCycle = 0;
		float cycleCompletedFraction;

		public LogisticsValue(int TotalToDeliver) {
			this.TotalThisCycle = TotalToDeliver;
			recalc();
		}
		
		public void deliverItem() {
			CompletedThisCycle++;
			recalc();
		}
		
		public void setCompleted() {
			CompletedThisCycle = TotalThisCycle;
			recalc();
		}
		
		private void recalc(){
			cycleCompletedFraction = TotalThisCycle == 0 ? 1F : (1F / (float) TotalThisCycle) * (float) CompletedThisCycle;
		}
	}
	
	private static HashMap<ItemIdentifier, HashMap<Router, LogisticsValue >>  _logisticsDatabase = new HashMap<ItemIdentifier, HashMap<Router,LogisticsValue>>();
	private static ILogisticsManager _instance;
	
	static { //Workaround to create instances of internal class
		_instance = new LogisticsManager();
	}
	
//	@Override
//	@Deprecated
//	public UUID getDestinationFor(ItemIdentifier item, Set<Router> validDestinations) {
//		if (!_logisticsDatabase.containsKey(item)){
//			_logisticsDatabase.put(item, new HashMap<Router, LogisticsValue>());
//		}
//		
//		HashMap<Router, LogisticsValue> itemEntry = _logisticsDatabase.get(item);
//		
//		Router nextDestination = null;
//		LogisticsValue nextDestinationValue = null;
//		
//		
//		boolean allCompleted = true;
//		for (Router r : validDestinations) {
//			if (!itemEntry.containsKey(r)){
//				CoreRoutedPipe pipe = r.getPipe();
//				if (pipe == null) continue;
//				if (!(pipe.logic instanceof LogicBasic)) continue;
//				itemEntry.put(r, new LogisticsValue(((LogicBasic)pipe.logic).RequestsItem(item)));
//			}
//			LogisticsValue value = itemEntry.get(r);
//			if (value.cycleCompletedFraction < 1F) {
//				allCompleted = false;
//				continue;
//			}
//		}
//		
//		if (allCompleted) {
//			for (Router r : validDestinations){
//				CoreRoutedPipe pipe = r.getPipe();
//				if (pipe == null) continue;
//				if (!(pipe.logic instanceof LogicBasic)) continue;
//				itemEntry.put(r, new LogisticsValue(((LogicBasic)pipe.logic).RequestsItem(item)));				
//			}
//		}
//		
//		for (Router r : validDestinations) {
//			LogisticsValue value = itemEntry.get(r);
//			CoreRoutedPipe pipe = r.getPipe();
//			if (pipe == null) continue;
//			if (!(pipe.logic instanceof LogicBasic)) continue;
//			//Ensure router still desires items
//			int requested = ((LogicBasic)pipe.logic).RequestsItem(item);
//			if (requested == 0){
//				value.setCompleted();
//			}
//			
//			if (value.cycleCompletedFraction == 1F)	{
//				continue;
//			}
//			
//			if (nextDestinationValue == null || value.cycleCompletedFraction < nextDestinationValue.cycleCompletedFraction)	{
//				nextDestinationValue = value;
//				nextDestination = r;
//			}
//		}
//		
//		if (nextDestinationValue != null){
//			nextDestinationValue.deliverItem();
//		}
//		return nextDestination!=null?nextDestination.id:null;
//	}

//	public static int getAvailableCount(ItemIdentifier item, Set<Router> validDestinations){
//		int count = 0;
//		for (Router r : validDestinations) {
//			if (r.getPipe() instanceof IProvideItems){
//				IProvideItems provider = (IProvideItems) r.getPipe();
//				count += provider.getAvailableItemCount(item);
//			}
//		}
//		return count;
//	}
	
	public static boolean Request(LogisticsRequest originalRequest, List<Router> validDestinations, List<ItemMessage> errors){
		return Request(originalRequest, validDestinations, errors, null);
	}
	
	
	public static boolean Request(LogisticsRequest originalRequest, List<Router> validDestinations, List<ItemMessage> errors, EntityPlayer player){
		LogisticsTransaction transaction = new LogisticsTransaction(originalRequest);
		return Request(transaction,validDestinations,errors,player);
	}

	public static boolean Request(LogisticsTransaction transaction, List<Router> validDestinations, List<ItemMessage> errors, EntityPlayer player){
		return Request(transaction,validDestinations,errors,player, true);
	}
	
	public static boolean Request(LogisticsTransaction transaction, List<Router> validDestinations, List<ItemMessage> errors, EntityPlayer player, boolean realrequest){
		//First check all crafters
		for (Router r : validDestinations) {
			if (r.getPipe() instanceof ICraftItems) {
				((ICraftItems)r.getPipe()).canCraft(transaction);
			}
		}
		boolean added = true;
		while(!transaction.isDeliverable() && added){
			//Then check if we can do this without crafting any items.
			
			//Then check if we can have it delivered
			for( Router r : validDestinations) {
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
			if(mod_LogisticsPipes.DisplayRequests)System.out.println("*** START REQUEST FOR " + transaction.getRequests().getFirst().numberLeft() + " " + transaction.getRequests().getFirst().getItem().getFriendlyName() + " ***");			
		}
		for (LogisticsRequest request : transaction.getRequests()){
			if(mod_LogisticsPipes.DisplayRequests)System.out.println("\tRequest for " + request.numberLeft() + " " + request.getItem().getFriendlyName());
			for(LogisticsPromise promise : request.getPromises()) {
				promise.sender.fullFill(promise, request.getDestination());
				if(mod_LogisticsPipes.DisplayRequests)System.out.println("\t\t" + getBetterRouterName(promise.sender.getRouter()) +  "\tSENDING " +promise.numberOfItems + " " + promise.item.getFriendlyName() + " to " + getBetterRouterName(request.getDestination().getRouter()));
				if (promise.extra){
					if(mod_LogisticsPipes.DisplayRequests)System.out.println("\t\t\t--Used extras from previous request");
				}
			}

			for (LogisticsPromise promise : request.getExtras()){
				if(mod_LogisticsPipes.DisplayRequests)System.out.println("\t\t\t--EXTRAS: " + promise.numberOfItems + " " + promise.item.getFriendlyName());
				//Register extras that can be used in later requests
				((ICraftItems)promise.sender).registerExtras(promise.numberOfItems);
			}
		}
		if(mod_LogisticsPipes.DisplayRequests)System.out.println("*** END REQUEST ***");
		
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
	public HashMap<ItemIdentifier, Integer> getAvailableItems(Set<Router> validDestinations) {
		HashMap<ItemIdentifier, Integer> allAvailableItems = new HashMap<ItemIdentifier, Integer>();
		for(Router r: validDestinations){
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
	public LinkedList<ItemIdentifier> getCraftableItems(Set<Router> validDestinations) {
		LinkedList<ItemIdentifier> craftableItems = new LinkedList<ItemIdentifier>();
		for (Router r : validDestinations){
			if (!(r.getPipe() instanceof ICraftItems)) continue;
			
			ICraftItems crafter = (ICraftItems) r.getPipe();
			ItemIdentifier craftedItem = crafter.getCraftedItem();
			if (craftedItem != null){
				craftableItems.add(craftedItem);
			}
		}
		return craftableItems;
	}
}
