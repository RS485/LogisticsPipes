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
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.utils.ItemIdentifierStack;


public class CraftingTemplate {
	
	private ItemIdentifierStack _result;
	private ICraftItems _crafter;
	private HashMap<ItemIdentifierStack, IRequestItems> _required = new HashMap<ItemIdentifierStack, IRequestItems>();
	
	public CraftingTemplate(ItemIdentifierStack result, ICraftItems crafter){
		_result = result;
		_crafter = crafter;
	}
	
	public void addRequirement(ItemIdentifierStack stack, IRequestItems crafter){
		_required.put(stack, crafter);
	}
	
	public LogisticsPromise generatePromise(){
		LogisticsPromise promise = new LogisticsPromise();
		promise.item = _result.getItem();
		promise.numberOfItems = _result.stackSize;
		promise.sender = _crafter;
		return promise;
	}
	
	public LinkedList<LogisticsRequest> generateRequests(){
		LinkedList<LogisticsRequest> requests = new LinkedList<LogisticsRequest>();
		for (ItemIdentifierStack stack : _required.keySet()){
			requests.add(new LogisticsRequest(stack.getItem(), stack.stackSize, _required.get(stack)));
		}
		return requests;
	}

	public ItemIdentifierStack getResultStack() {
		return _result;
	}
	
	public ICraftItems getCrafter(){
		return _crafter;
	}

}
