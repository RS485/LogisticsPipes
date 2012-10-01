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

import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IProvideItems;
import logisticspipes.request.CraftingTemplate;
import logisticspipes.utils.ItemIdentifier;


public class LogisticsTransaction {

	private LinkedList<LogisticsRequest> _requests = new LinkedList<LogisticsRequest>();
	private LinkedList<CraftingTemplate> _craftingTemplates = new LinkedList<CraftingTemplate>();
	
	private LinkedList<LogisticsPromise> _promises = new LinkedList<LogisticsPromise>();
	
	private boolean realRequest;

	public LogisticsTransaction (LogisticsRequest originalRequest, boolean realRequest){
		_requests.add(originalRequest);
		this.realRequest = realRequest;
	}

	public LogisticsTransaction (boolean realRequest) {
		this.realRequest = realRequest;
	}
	
	public LinkedList<LogisticsRequest> getRequests(){
		return (LinkedList<LogisticsRequest>) _requests;
	}
	
	public void addCraftingTemplate(CraftingTemplate template){
		template.setRealRequest(realRequest);
		_craftingTemplates.add(template);
	}
	
	public boolean hasCraftingTemplates() {
		return _craftingTemplates.size() > 0;
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
		for(LogisticsPromise promise : _promises){
			if (promise.sender != sender) continue;
			if (!ret.containsKey(promise.item)) {
				ret.put(promise.item, promise.numberOfItems);
				continue;
			}
			ret.put(promise.item, ret.get(promise.item) + promise.numberOfItems);	
		}
		return ret;
	}

	public void addRequest(LogisticsRequest newRequest) {
		if (_requests.contains(newRequest)){
			return;
		}
		_requests.add(newRequest);
	}

	public void removeRequest(LogisticsRequest localRemain) {
		_requests.remove(localRemain);
	}

	public LogisticsTransaction copyWithoutCrafter(ICraftItems crafter) {
		LogisticsTransaction copy = new LogisticsTransaction(realRequest);
		for(LogisticsRequest request : _requests) {
			for(LogisticsPromise promise : request.getPromises()){
				copy._promises.add(promise);
			}
		}
		for(LogisticsPromise promise : this._promises){
			copy._promises.add(promise);
		}
		for (CraftingTemplate template : _craftingTemplates){
			if(!template.getCrafter().equals(crafter)) {
				copy._craftingTemplates.add(template);
			}
		}
		return copy;
	}

	public void setRealRequest(boolean realRequest) {
		this.realRequest = realRequest;
	}

	public void insertRequests(LogisticsTransaction newtransaction) {
		this._requests.addAll(newtransaction._requests);
	}

	public void insertPromises(LogisticsTransaction lastUsedTransaction) {
		for(LogisticsRequest request : lastUsedTransaction._requests) {
			for(LogisticsPromise promise : request.getPromises()){
				this._promises.add(promise);
			}
		}
	}
}
