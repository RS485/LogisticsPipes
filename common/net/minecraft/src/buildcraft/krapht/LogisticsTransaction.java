/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.src.krapht.ItemIdentifier;

public class LogisticsTransaction {

	private LinkedList<LogisticsRequest> _requests = new LinkedList<LogisticsRequest>();
	private LinkedList<CraftingTemplate> _craftingTemplates = new LinkedList<CraftingTemplate>();

	public LogisticsTransaction (LogisticsRequest originalRequest){
		_requests.add(originalRequest);
	}

	public LogisticsTransaction () {
	}
	
	public LinkedList<LogisticsRequest> getRequests(){
		return (LinkedList<LogisticsRequest>) _requests;
	}
	
	public void addCraftingTemplate(CraftingTemplate template){
		_craftingTemplates.add(template);
	}
	
	public boolean isDeliverable(){
		boolean allReady = true;
		for (LogisticsRequest request : _requests) {
			allReady = allReady && request.isReady();
		}
		return allReady;
	}
	
	public boolean hasCraft(ItemIdentifier item) {
		for (CraftingTemplate template : _craftingTemplates){
			if (template.getResultStack().getItem() == item) return true;
		}
		return false;
	}
	
	public LinkedList<CraftingTemplate> getCrafts(ItemIdentifier item){
		LinkedList<CraftingTemplate> templates = new LinkedList<CraftingTemplate>();
		for(CraftingTemplate template : _craftingTemplates){
			if (template.getResultStack().getItem() == item) {
				templates.add(template);
			}
		}
		return templates;
	}
	
	public LinkedList<LogisticsRequest> getRemainingRequests(){
		LinkedList<LogisticsRequest> ret = new LinkedList<LogisticsRequest>();
		for (LogisticsRequest request : _requests){
			if (!request.isReady()){
				ret.add(request);
			}
		}
		return ret;
	}
	
	public HashMap<ItemIdentifier, Integer> getTotalPromised(IProvideItems sender){
		HashMap<ItemIdentifier, Integer> ret = new HashMap<ItemIdentifier, Integer>();
		for(LogisticsRequest request : _requests) {
			for(LogisticsPromise promise : request.getPromises()){
				if (promise.sender != sender) continue;
				if (!ret.containsKey(promise.item)) {
					ret.put(promise.item, promise.numberOfItems);
					continue;
				}
				ret.put(promise.item, ret.get(promise.item) + promise.numberOfItems);	
			}
		}
		return ret;
	}

	public void addRequest(LogisticsRequest newRequest) {
		if (_requests.contains(newRequest)){
			return;
		}
		_requests.add(newRequest);
	}

}
