/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.ISaveState;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class SimpleInventory implements IInventory, ISaveState{

	private ItemStack[] _contents;
	private final String _name;
	private final int _stackLimit;
	private final HashMap<ItemIdentifier, Integer> _contentsMap;
	private final HashSet<ItemIdentifier> _contentsUndamagedSet;
	
	private final LinkedList<ISimpleInventoryEventHandler> _listener = new LinkedList<ISimpleInventoryEventHandler>(); 
	
	public SimpleInventory(int size, String name, int stackLimit){
		_contents = new ItemStack[size];
		_name = name;
		_stackLimit = stackLimit;
		_contentsMap = new HashMap<ItemIdentifier, Integer>((int)(size * 1.5));
		_contentsUndamagedSet = new HashSet<ItemIdentifier>((int)(size * 1.5));
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
		if (_contents[i].stackSize > j) {
			ItemStack ret = _contents[i].splitStack(j);
			updateContents();
			return ret;
		}
		ItemStack ret = _contents[i];
		_contents[i] = null;
		updateContents();
		return ret;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		_contents[i] = itemstack;
		updateContents();
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
		updateContents();
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
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		readFromNBT(nbttagcompound, "");
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {
		NBTTagList nbttaglist = nbttagcompound.getTagList(prefix + "items");
    	
    	for (int j = 0; j < nbttaglist.tagCount(); ++j) {    		
    		NBTTagCompound nbttagcompound2 = (NBTTagCompound) nbttaglist.tagAt(j);
    		int index = nbttagcompound2.getInteger("index");
    		if(index < _contents.length) {
    			_contents [index] = ItemStack.loadItemStackFromNBT(nbttagcompound2);
    		} else {
    			LogisticsPipes.log.severe("SimpleInventory: java.lang.ArrayIndexOutOfBoundsException: " + index + " of " + _contents.length);
    		}
    	}
		updateContents();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		writeToNBT(nbttagcompound, "");
	}

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
    	nbttagcompound.setInteger(prefix + "itemsCount", _contents.length);
	}

	public void dropContents(World worldObj, int posX, int posY, int posZ) {
		if(MainProxy.isServer(worldObj)) {
			for(int i=0;i<_contents.length;i++) {
				while(_contents[i] != null) {
					ItemStack todrop = decrStackSize(i, _contents[i].getMaxStackSize());
			    	dropItems(worldObj, todrop, posX, posY, posZ);
				}
			}
			updateContents();
		}
	}

	private static void dropItems(World world, ItemStack stack, int i, int j, int k) {
		if(stack.stackSize <= 0)
			return;
		float f1 = 0.7F;
		double d = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
		double d1 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
		double d2 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
		EntityItem entityitem = new EntityItem(world, i + d, j + d1, k + d2, stack);
		entityitem.delayBeforeCanPickup = 10;
		world.spawnEntityInWorld(entityitem);
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
		updateContents();
		return stackToTake;
	}

	public void handleItemIdentifierList(Collection<ItemIdentifierStack> _allItems) {
		int i=0;
		for(ItemIdentifierStack stack:_allItems) {
			if(_contents.length <= i) break;
			if(stack == null) {
				_contents[i] = null;
			} else {
				_contents[i] = stack.unsafeMakeNormalStack();
			}
			i++;
		}
		onInventoryChanged();
	}
	
	private int tryAddToSlot(int i, ItemStack stack) {
		ItemStack slot = _contents[i];
		if(slot == null) {
			_contents[i] = stack.copy();
			return stack.stackSize;
		}
		ItemIdentifier slotIdent = ItemIdentifier.get(slot);
		ItemIdentifier stackIdent = ItemIdentifier.get(stack);
		if(slotIdent.equals(stackIdent)) {
			slot.stackSize += stack.stackSize;
			if(slot.stackSize > 127) {
				int ans = stack.stackSize - (slot.stackSize - 127);
				slot.stackSize = 127;
				return ans;
			} else {
				return stack.stackSize;
			}
		} else {
			return 0;
		}
	}
	
	public int addCompressed(ItemStack stack) {
		stack = stack.copy();
		for(int i=0; i<this._contents.length;i++) {
			if(stack.stackSize <= 0) {
				break;
			}
			int added = tryAddToSlot(i, stack);
			stack.stackSize -= added;
		}
		onInventoryChanged();
		return stack.stackSize;
	}

	/* InventoryUtil-like functions */

	private void updateContents() {
		_contentsMap.clear();
		_contentsUndamagedSet.clear();
		for (int i = 0; i < _contents.length; i++) {
			ItemStack stack = _contents[i];
			if (stack == null) {
				continue;
			}
			ItemIdentifier itemId = ItemIdentifier.get(stack);
			Integer count = _contentsMap.get(itemId);
			if (count == null) {
				_contentsMap.put(itemId, stack.stackSize);
			} else {
				_contentsMap.put(itemId, _contentsMap.get(itemId) + stack.stackSize);
			}
			ItemIdentifier itemUndamagedId = ItemIdentifier.getUndamaged(stack);
			_contentsUndamagedSet.add(itemUndamagedId); // add is cheaper than check then add; it just returns false if it is already there
		}
	}

	public int itemCount(final ItemIdentifier item) {
		Integer i =  _contentsMap.get(item);
		if(i == null) 
			return 0;
		return i;
	}

	public Map<ItemIdentifier, Integer> getItemsAndCount() {
		return _contentsMap;
	}

	public boolean containsItem(final ItemIdentifier item) {
		return _contentsMap.containsKey(item);
	}

	public boolean containsUndamagedItem(final ItemIdentifier item) {
		return _contentsUndamagedSet.contains(item);
	}

	public boolean isEmpty() {
		return _contentsMap.isEmpty();
	}

	@Override
	public boolean isInvNameLocalized() {
		// TODO ?
		return true;
	}

	@Override
	public boolean isStackValidForSlot(int i, ItemStack itemstack) {
		// TODO Auto-generated method stub
		return false;
	}
}
