/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht;

import java.util.LinkedList;

import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.krapht.ItemIdentifier;

public class LogisticsRequest {
	private ItemIdentifier _item;
	private int _count;
	private IRequestItems _destination;
	
	private LinkedList<LogisticsPromise> _promises = new LinkedList<LogisticsPromise>();
	private LinkedList<LogisticsPromise> _extraPromises = new LinkedList<LogisticsPromise>();
	
	public LogisticsRequest(ItemIdentifier item, int numberOfItems, IRequestItems destination){
		this._item = item;
		this._count = numberOfItems;
		this._destination = destination;
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
		if(mod_LogisticsPipes.DisplayRequests)System.out.println("Adding promise of " + promise.numberOfItems + " " + promise.item.getFriendlyName());
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
			if(mod_LogisticsPipes.DisplayRequests)System.out.println("\treduced promise to " + promise.numberOfItems);
			if(mod_LogisticsPipes.DisplayRequests)System.out.println("\tAdding EXTRA promise of " + extrasPromise.numberOfItems + " " + extrasPromise.item.getFriendlyName());
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
			if(mod_LogisticsPipes.DisplayRequests)System.out.println("\tUsing promise of " + promise.numberOfItems + " " + promise.item.getFriendlyName());
			_extraPromises.remove(promise);
		}
	}
}


