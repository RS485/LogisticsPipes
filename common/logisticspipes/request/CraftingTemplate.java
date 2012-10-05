/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;


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
	
	public List<Pair<ItemIdentifierStack,IRequestItems>> getSource() {
		List<Pair<ItemIdentifierStack,IRequestItems>> result = new ArrayList<Pair<ItemIdentifierStack,IRequestItems>>();
		for (ItemIdentifierStack stack : _required.keySet()) {
			result.add(new Pair<ItemIdentifierStack,IRequestItems>(stack,_required.get(stack)));
		}
		return result;
	}

	public ItemIdentifierStack getResultStack() {
		return _result;
	}
	
	public ICraftItems getCrafter(){
		return _crafter;
	}

}
