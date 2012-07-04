/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.krapht;

import java.util.LinkedList;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.krapht.ISaveState;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;

public class SimpleInventory implements IInventory, ISaveState{
	
	private final ItemStack[] _contents;
	private final String _name;
	private final int _stackLimit;
	
	private final LinkedList<ISimpleInventoryEventHandler> _listener = new LinkedList<ISimpleInventoryEventHandler>(); 
	
	public SimpleInventory(int size, String name, int stackLimit){
		_contents = new ItemStack[size];
		_name = name;
		_stackLimit = stackLimit;
	}
	
	@Override
	public int getSizeInventory() {
		return _contents.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return _contents[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if (_contents[i] == null) return null;
		if (_contents[i].stackSize > j) return _contents[i].splitStack(j);
		ItemStack ret = _contents[i];
		_contents[i] = null;
		return ret;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		_contents[i] = itemstack;
		
	}

	@Override
	public String getInvName() {
		return _name;
	}

	@Override
	public int getInventoryStackLimit() {
		return _stackLimit;
	}

	@Override
	public void onInventoryChanged() {
		for (ISimpleInventoryEventHandler handler : _listener){
			handler.InventoryChanged(this);
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {return false;}

	@Override
	public void openChest() {}

	@Override
	public void closeChest() {}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {
		NBTTagList nbttaglist = nbttagcompound.getTagList(prefix + "items");
    	
    	for (int j = 0; j < nbttaglist.tagCount(); ++j) {    		
    		NBTTagCompound nbttagcompound2 = (NBTTagCompound) nbttaglist.tagAt(j);
    		int index = nbttagcompound2.getInteger("index");
    		_contents [index] = ItemStack.loadItemStackFromNBT(nbttagcompound2);
    	}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {
		NBTTagList nbttaglist = new NBTTagList();
    	for (int j = 0; j < _contents.length; ++j) {    		    		
    		if (_contents[j] != null && _contents[j].stackSize > 0) {
        		NBTTagCompound nbttagcompound2 = new NBTTagCompound ();
        		nbttaglist.appendTag(nbttagcompound2);
    			nbttagcompound2.setInteger("index", j);
    			_contents[j].writeToNBT(nbttagcompound2);	
    		}     		
    	}
    	nbttagcompound.setTag(prefix + "items", nbttaglist);
	}

	public void dropContents(World worldObj, int xCoord, int yCoord, int zCoord) {
		if(!APIProxy.isRemote()) {
			SimpleServiceLocator.buildCraftProxy.dropItems(worldObj, this, xCoord, yCoord, zCoord);
			for(int i=0;i<_contents.length;i++) {
				_contents[i] = null;
			}
		}
	}
	
	public void addListener(ISimpleInventoryEventHandler listner){
		if (!_listener.contains(listner)){
			_listener.add(listner);
		}
	}
	
	public void removeListener(ISimpleInventoryEventHandler listner){
		if (_listener.contains(listner)){
			_listener.remove(listner);
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		if (this._contents[i] == null) return null;
		
		ItemStack stackToTake = this._contents[i];
		this._contents[i] = null;
		return stackToTake;
	}
}
