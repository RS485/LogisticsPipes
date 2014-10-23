/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing.order;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import logisticspipes.interfaces.IChangeListener;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.order.IOrderInfoProvider.RequestType;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.world.World;

public class LogisticsOrderManager implements Iterable<LogisticsOrder> {
		
	public LogisticsOrderManager() {
	}
	
	public LogisticsOrderManager(IChangeListener listener) {
		this.listener = listener;
	}
	
	private LinkedList<LogisticsOrder>	_orders		= new LinkedList<LogisticsOrder>();
	private IChangeListener				listener	= null;
	
	private void listen() {
		if(listener != null) {
			listener.listenedChanged();
		}
	}
	
	public LinkedList<ItemIdentifierStack> getContentList(World world) {
		if(MainProxy.isClient(world) || _orders.size() == 0) return new LinkedList<ItemIdentifierStack>();
		LinkedList<ItemIdentifierStack> list = new LinkedList<ItemIdentifierStack>();
		for(LogisticsOrder request: _orders) {
			addToList(request.getItem(), list);
		}
		return list;
	}
	
	private static void addToList(ItemIdentifierStack stack, LinkedList<ItemIdentifierStack> list) {
		for(ItemIdentifierStack ident: list) {
			if(ident.getItem().equals(stack.getItem())) {
				ident.setStackSize(ident.getStackSize() + stack.getStackSize());
				return;
			}
		}
		list.addLast(stack.clone());
	}
	
	public boolean hasOrders(RequestType type) {
		return  peekAtTopRequest(type)!=null;
	}
	
/*	public LogisticsOrder peekAtTopRequest() {
		return _orders.getFirst().setInProgress(true);
	}*/

	/* only multi-access SAFE when type is null; all other access patterns may change the state of the stack so the returned element is on top*/
	public LogisticsOrder peekAtTopRequest(RequestType type) {
		if(_orders.size()==0)
			return null;
		LogisticsOrder top = _orders.getFirst().setInProgress(true);
		int loopCount=0;
		while(type != null & top.getType()!=type){
			loopCount++;
			if(loopCount>_orders.size()) {
				return null;
				//throw new NoSuchElementException("Unable to find a Request of Type "+type.toString());
			}
			deferSend(); // sets the new top to InProgress
			top = _orders.getFirst();
		}
		return top;
	}

	public void sendSuccessfull(int number, boolean defersend, IRoutedItem item) {
		_orders.getFirst().getItem().setStackSize(_orders.getFirst().getItem().getStackSize() - number);
		if(_orders.getFirst().isWatched()) {
			IDistanceTracker tracker = new DistanceTracker();
			item.setDistanceTracker(tracker);
			_orders.getFirst().addDistanceTracker(tracker);
		}
		if(_orders.getFirst().getItem().getStackSize() <= 0) {
			LogisticsOrder order = _orders.removeFirst();
			order.setFinished(true);
			order.setInProgress(false);
		} else if(defersend) {
			_orders.add(_orders.removeFirst().setInProgress(false));
			_orders.getFirst().setInProgress(true);
		}
		listen();
	}
	
	public void sendFailed() {
		_orders.getFirst().getDestination().itemCouldNotBeSend(_orders.getFirst().getItem(), _orders.getFirst().getInformation());
		if(!_orders.isEmpty()) {
			LogisticsOrder order = _orders.removeFirst();
			order.setFinished(true);
			order.setInProgress(false);
		}
		if(!_orders.isEmpty()) {
			_orders.getFirst().setInProgress(true);
		}
		listen();
	}
	
	public void deferSend() {
		_orders.add(_orders.removeFirst().setInProgress(false));
		_orders.getFirst().setInProgress(true);
		listen();
	}
	
	public LogisticsOrder addOrder(ItemIdentifierStack stack, IRequestItems requester, RequestType type, IAdditionalTargetInformation info) {
		LogisticsOrder order = new LogisticsOrder(stack, requester, type, info);
		_orders.addLast(order);
		listen();
		return order;
	}
	
	public int totalItemsCountInOrders(ItemIdentifier item) {
		int itemCount = 0;
		for(LogisticsOrder request: _orders) {
			if(!request.getItem().getItem().equals(item)) continue;
			itemCount += request.getItem().getStackSize();
		}
		return itemCount;
	}
	
	public int totalItemsCountInAllOrders() {
		int itemCount = 0;
		for(LogisticsOrder request: _orders) {
			itemCount += request.getItem().getStackSize();
		}
		return itemCount;
	}

	public void setMachineProgress(byte progress) {
		if(_orders.isEmpty()) return;
		_orders.getFirst().setMachineProgress(progress);
	}

	public boolean isFirstOrderWatched() {
		if(_orders.isEmpty()) return false;
		return _orders.getFirst().isWatched();
	}

	/**
	 * DON'T MODIFY TROUGH THIS ONLY READ THE VALUES
	 */
	@Override
	public Iterator<LogisticsOrder> iterator() {
		return Collections.unmodifiableList(this._orders).iterator();
	}
}
