/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.main;

import java.util.LinkedList;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.utils.ItemIdentifier;



public class LogisticsRequest {
	private ItemIdentifier _item;
	private int _count;
	private IRequestItems _destination;
	
	private LinkedList<LogisticsPromise> _promises = new LinkedList<LogisticsPromise>();
	private LinkedList<LogisticsPromise> _extraPromises = new LinkedList<LogisticsPromise>();
	
	public boolean realRequest;
	
	public LogisticsRequest(ItemIdentifier item, int numberOfItems, IRequestItems destination, boolean realRequest){
		this._item = item;
		this._count = numberOfItems;
		this._destination = destination;
		this.realRequest = realRequest;
	}
	
	public ItemIdentifier getItem(){
		return _item;
	}
	
	public int numberLeft(){
		return _count;
	}
	
	public void reduceNumberLeft(){
		_count--;
	}
	public void reduceNumberLeft(int count){
		_count -= count;		
	}
	
	public int notYetAllocated(){
		int totalAllocated = 0;
		for (LogisticsPromise promise : _promises){
			totalAllocated += promise.numberOfItems;
		}
		return _count - totalAllocated;
	}
	public boolean isReady(){
		return notYetAllocated() < 1;
	}
	
	public boolean isComplete() {
		return _count < 1;
	}
	
	public IRequestItems getDestination(){
		return _destination;
	}
	
	public void addPromise(LogisticsPromise promise){
		if(LogisticsPipes.DisplayRequests && realRequest)System.out.println("Adding promise of " + promise.numberOfItems + " " + promise.item.getFriendlyName());
		if (promise.numberOfItems < 1)
			return;
		//Ensure promise never exceeds what we need
		if (promise.numberOfItems > notYetAllocated()){
			LogisticsPromise extrasPromise = new LogisticsPromise();
			extrasPromise.numberOfItems = promise.numberOfItems - notYetAllocated();
			extrasPromise.item = promise.item;
			extrasPromise.sender = promise.sender;
			promise.numberOfItems = notYetAllocated();
			_extraPromises.add(extrasPromise);
			if(LogisticsPipes.DisplayRequests && realRequest)System.out.println("\treduced promise to " + promise.numberOfItems);
			if(LogisticsPipes.DisplayRequests && realRequest)System.out.println("\tAdding EXTRA promise of " + extrasPromise.numberOfItems + " " + extrasPromise.item.getFriendlyName());
		}
		_promises.add(promise);
	}
	
	public LinkedList<LogisticsPromise> getPromises(){
		return _promises;
	}
	
	public int getExtrasCount(){
		int count = 0;
		for (LogisticsPromise extra : _extraPromises){
			count+=extra.numberOfItems;
		}
		return count;
	}

	public LinkedList<LogisticsPromise> getExtras() {
		return (LinkedList<LogisticsPromise>) _extraPromises.clone();
	}
	
	public void usePromise(LogisticsPromise promise){
		if (_extraPromises.contains(promise)){
			if(LogisticsPipes.DisplayRequests)System.out.println("\tUsing promise of " + promise.numberOfItems + " " + promise.item.getFriendlyName());
			_extraPromises.remove(promise);
		}
	}

	public LogisticsRequest copy() {
		LogisticsRequest request = new LogisticsRequest(_item, _count, _destination, realRequest);
		for(LogisticsPromise promise:_promises) {
			request._promises.add(promise.copy());
		}
		for(LogisticsPromise promise:_extraPromises) {
			request._extraPromises.add(promise.copy());
		}
		return request;
	}
	
	public boolean equals(LogisticsRequest part) {
		return part._count == this._count && part._destination.equals(this._destination) && part._item.equals(this._item);
	}
	
	public String toString() {
		return _count + "x" + _item.getDebugName();
	}
}


