/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.buildcraft.krapht;

import java.util.LinkedList;

import logisticspipes.krapht.ItemIdentifier;


public class LogisticsOrderManager {

	private LinkedList<LogisticsRequest> _orders = new LinkedList<LogisticsRequest>();
		
	public boolean hasOrders(){
		return _orders.size() > 0;
	}
	
	public LogisticsRequest getNextRequest(){
		return _orders.getFirst();
	}
	
	public void sendSuccessfull(int number){
		_orders.getFirst().reduceNumberLeft(number);
		if (_orders.getFirst().isComplete()){
			_orders.removeFirst();
		}
	}

	public void sendFailed() {
		//TODO: Notify logisticsmanager that order cannot be met, for now, remove the order
		if (!_orders.isEmpty()){
			_orders.removeFirst();
		}
	}

	public void addOrder(LogisticsRequest order) {
		_orders.addLast(order);
	}
	
	public int totalItemsCountInOrders(ItemIdentifier item){
		int itemCount = 0;
		for (LogisticsRequest request : _orders){
			if (request.getItem() != item) continue;
			itemCount += request.numberLeft();
		}
		return itemCount;
	}
	
}
