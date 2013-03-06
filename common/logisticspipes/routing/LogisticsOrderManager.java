/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.LinkedList;
import java.util.List;

import logisticspipes.interfaces.IChangeListener;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;
import logisticspipes.utils.Pair3;


public class LogisticsOrderManager {
	
	public LogisticsOrderManager() {}
	
	public LogisticsOrderManager(IChangeListener listener) {
		this.listener = listener;
	}
	
	//private LinkedList<LogisticsRequest> _orders = new LinkedList<LogisticsRequest>();
	private LinkedList<Pair3<ItemIdentifierStack,IRequestItems,List<IRelayItem>>> _orders = new LinkedList<Pair3<ItemIdentifierStack,IRequestItems,List<IRelayItem>>>();
	private IChangeListener listener = null;
	
	private void listen() {
		if(listener != null) {
			listener.listenedChanged();
		}
	}
	
	public LinkedList<ItemIdentifierStack> getContentList() {
		if(MainProxy.isClient() || _orders.size()==0) return new LinkedList<ItemIdentifierStack>();
		LinkedList<ItemIdentifierStack> list = new LinkedList<ItemIdentifierStack>();
		for (Pair<ItemIdentifierStack,IRequestItems> request : _orders){
			addToList(request.getValue1(),list);
		}
		return list;
	}

	private static void addToList(ItemIdentifierStack stack, LinkedList<ItemIdentifierStack> list) {
		for(ItemIdentifierStack ident:list) {
			if(ident.getItem().equals(stack.getItem())) {
				ident.stackSize += stack.stackSize;
				return;
			}
		}
		list.addLast(stack.clone());
	}
	
	public boolean hasOrders(){
		return _orders.size() > 0;
	}
	
	public Pair3<ItemIdentifierStack,IRequestItems,List<IRelayItem>> getNextRequest(){
		return _orders.getFirst();
	}
	
	public void sendSuccessfull(int number, boolean defersend) {
		_orders.getFirst().getValue1().stackSize -= number;
		if (_orders.getFirst().getValue1().stackSize <= 0){
			_orders.removeFirst();
		} else if(defersend) {
			_orders.add(_orders.removeFirst());
		}
		listen();
	}

	public void sendFailed() {
		_orders.getFirst().getValue2().itemCouldNotBeSend(_orders.getFirst().getValue1());
		if (!_orders.isEmpty()){
			_orders.removeFirst();
		}
		listen();
	}

	public void deferSend() {
		_orders.add(_orders.removeFirst());
		listen();
	}

	public void addOrder(ItemIdentifierStack stack, IRequestItems requester, List<IRelayItem> relays) {
		for (Pair3<ItemIdentifierStack,IRequestItems,List<IRelayItem>> request : _orders){
			if (request.getValue1().getItem() == stack.getItem() && request.getValue2() == requester) {
				stack.stackSize += request.getValue1().stackSize;
				_orders.remove(request);
				break;
			}
		}
		_orders.addLast(new Pair3<ItemIdentifierStack,IRequestItems, List<IRelayItem>>(stack, requester, relays));
		listen();
	}
	
	public int totalItemsCountInOrders(ItemIdentifier item){
		int itemCount = 0;
		for (Pair<ItemIdentifierStack,IRequestItems> request : _orders){
			if (request.getValue1().getItem() != item) continue;
			itemCount += request.getValue1().stackSize;
		}
		return itemCount;
	}

	public int totalItemsCountInAllOrders(){
		int itemCount = 0;
		for (Pair<ItemIdentifierStack,IRequestItems> request : _orders){
			itemCount += request.getValue1().stackSize;
		}
		return itemCount;
	}
	
}
